# 0003. 예약 대기 승격은 승격 대상 waiting row에 비관적 락을 잡는다

- 상태: Accepted
- 날짜: 2026-06-04
- 관련 코드: `WaitingDao.findFirstBySlot`, `ReservationService.promoteFirstWaiting`, `WaitingService.delete`

## Context

ADR-0002에서 결정한 바에 따라 예약 대기 승격은 호출자(`cancelReservation` / `removeReservation` / `changeReservationSlot`)의 트랜잭션 안에서 다음 흐름으로 진행된다.

```
reservationDao.delete(...)
findFirstBySlot(date, time, theme)   ← 1번 대기자 W1 조회
reservationDao.save(new Reservation(W1.name, ...))
waitingDao.delete(W1.id)
```

이 흐름은 트랜잭션의 원자성으로 "save + delete"가 같이 commit/rollback 되는 것은 보장하지만, **`findFirstBySlot`과 이후 동작 사이에 W1 row가 외부 트랜잭션에 의해 변경되는 race**는 보호하지 못한다.

### 실제로 발생할 수 있는 race

```
시점       T1 (사용자 A의 cancelReservation)        T2 (사용자 B의 waiting 취소)
─────────────────────────────────────────────────────────────────────────────────
t0         reservationDao.delete(reservationId)
t1         findFirstBySlot(...) → W1 발견
t2                                                    waitingDao.delete(W1)  ← 끼어듦
t3         reservationDao.save(W1 → reservation)
t4         waitingDao.delete(W1)  ← 이미 없음, no-op
─────────────────────────────────────────────────────────────────────────────────
결과: 사용자 B는 "대기 취소"를 요청했으나 자기 대기가 예약으로 승격되어 버림
```

이 race는 `reservation` 테이블의 `UNIQUE (date, time_id, theme_id)` 제약으로 막을 수 없다. UNIQUE 제약은 "슬롯 중복"만 검증하기 때문이다. **같은 W1 row를 대상으로 한 두 다른 의도("승격" vs "취소")의 충돌은 DB가 알 길이 없다.**

## Decision

`WaitingDao.findFirstBySlot`이 1번 대기자 row를 조회할 때 **`SELECT ... FOR UPDATE`로 행 단위 비관적 락**을 잡는다.

```sql
SELECT w.id, w.name, w.date, w.created_at, ...
FROM waiting w
INNER JOIN reservation_time rt ON w.time_id = rt.id
INNER JOIN theme t ON w.theme_id = t.id
WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
ORDER BY w.created_at, w.id ASC
LIMIT 1
FOR UPDATE
```

이 락은 호출자 트랜잭션이 commit/rollback 될 때까지 유지되며, 동일 W1 row를 변경하려는 다른 트랜잭션(또 다른 promote, 또는 `WaitingService#delete`)을 자동으로 직렬화한다.

## Rationale

비관적 락 + 트랜잭션의 조합으로 promote는 **원자적 상태 전이**를 보장한다. 가능한 결과 상태가 세 가지로 좁혀진다.

| 상태 | 의미 | DB 변화 |
|------|------|---------|
| (a) 승격 실행 | 1번 대기자 W1이 reservation으로 옮겨감 | reservation INSERT + W1 DELETE 모두 commit |
| (b) 승격 skip | 대기자 없음 / W1이 직전에 사라짐 / 과거 슬롯 가드 | DB 변화 없음 |
| (c) 전체 롤백 | timeout / deadlock / 외부 예외 | 상위 트랜잭션까지 모두 무효 |

(a)와 (b)는 의도된 정상 흐름이다. (c)는 시스템 레벨 실패로, "중간 상태 commit"이 아니라 "처음부터 아무 일도 일어나지 않음"에 해당한다.

즉 **"승격이 절반만 성공한 상태"는 존재하지 않는다.** 도메인 관점에서는 (a) / (b) 두 가지만 신경 쓰면 된다.

### 왜 공유 락(`FOR SHARE`)이 아니라 배타 락(`FOR UPDATE`)인가

공유 락도 우리가 본 1:1 race(promote vs 사용자 waiting 취소)는 직렬화해 준다. T2의 `DELETE`가 자동 X-lock을 요구하는데 T1이 보유한 S-lock과 충돌해 블록되기 때문이다.

문제는 **동시에 두 promote가 같은 W1을 보는 경우**다.

```
T1: SELECT FOR SHARE W1 → S-lock 획득
T3: SELECT FOR SHARE W1 → S-lock 획득 (S-lock끼리는 호환 → 둘 다 보유)
T1: DELETE W1 → X-lock 업그레이드 시도 → T3의 S-lock 대기
T3: DELETE W1 → X-lock 업그레이드 시도 → T1의 S-lock 대기
→ 서로 대기 → DEADLOCK
```

promote는 조회 후 같은 row를 반드시 `DELETE`까지 가는 흐름이라, S-lock으로 시작하면 끝에서 X-lock 업그레이드가 강제된다. 동시 진입이 있으면 deadlock 위험을 만들고, DB가 한쪽을 강제 롤백시킨다. `FOR UPDATE`로 처음부터 X-lock을 잡으면 동시 진입 시점에 한 명만 락을 획득하고 나머지는 조용히 대기한다.

### 미해결 — JOIN 락 범위 좁히기

현재 쿼리는 `reservation_time` / `theme`까지 JOIN한 상태에서 `FOR UPDATE`가 걸려 있다. JOIN된 테이블 row까지 락 대상이 되는 것 같아 보였는데, 그렇다면 다른 트랜잭션의 같은 time/theme 행 변경이 영향을 받을 수 있는지가 의문으로 남았다.

본 ADR 시점에는 락의 *종류*(S/X)와 락의 *범위*가 독립적인 축이라는 점, 그리고 좁히려면 `FOR UPDATE OF w`나 JOIN 분리를 검토해야 한다는 방향만 인지했고, 정확한 동작과 부작용 정도는 결론 내리지 못했다. 실제 부작용이 관찰되거나 다른 RDB로 옮길 때 다시 검토한다.

## Consequences

긍정:
- promote 도중 대상 waiting의 외부 변경(특히 같은 W1을 대상으로 한 사용자 취소 / 또 다른 promote 시도)이 직렬화된다.
- 동시 cancel/remove로 인한 `reservation` UNIQUE 충돌이 락 단계에서 자연스럽게 해결된다.
- 결과 상태가 (a) / (b) / (c) 셋으로 명확히 좁혀져, 도메인 코드가 "어중간한 상태" 처리를 고려할 필요가 없다.
- **일반 조회는 영향받지 않는다.** H2는 기본 MVCC 모드에서 일반 `SELECT`가 락을 보지 않고 스냅샷을 읽으므로, promote 트랜잭션이 X-lock을 보유한 시간 동안에도 화면 조회는 블록 없이 통과한다.

부정/주의:
- 락 자체가 새로운 실패 양상(`JdbcSQLTimeoutException`, deadlock 감지로 인한 롤백)을 만든다. 락이 없을 때 발생하지 않던 종류의 예외가 생긴다.
- H2의 `SELECT ... FOR UPDATE`는 row-lock이라 promote 도중 같은 슬롯에 새 waiting이 INSERT 되는 것은 막지 못한다. 단, 새 waiting은 다음 promote의 대상이 될 뿐이라 의미상 문제는 아니다.