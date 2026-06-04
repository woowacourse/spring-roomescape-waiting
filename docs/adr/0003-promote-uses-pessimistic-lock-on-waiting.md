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

### 왜 이게 두 메커니즘의 합인가

- **트랜잭션 (원자성)**: `reservation.save`와 `waiting.delete`가 한 단위로 commit. 한쪽만 적용되는 상태는 commit되지 않음.
- **비관적 락 (격리)**: W1 row가 트랜잭션 동안 다른 트랜잭션에 의해 변경/삭제되지 않음. "있는 줄 알고 save 했는데 사실 사라진 상태"가 발생하지 않음.

두 보장이 합쳐져야 위 표의 세 결과로 좁혀진다. 트랜잭션만으로는 (b)와 race가 구분되지 않고, 락만으로는 부분 commit이 가능해진다.

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

promote는 조회 후 같은 row를 반드시 `DELETE`까지 가는 흐름이라, S-lock으로 시작하면 끝에서 X-lock 업그레이드가 강제된다. 동시 진입이 있으면 deadlock 위험을 만들고, DB가 한쪽을 강제 롤백시킨다.

`FOR UPDATE`로 처음부터 X-lock을 잡으면 동시 진입 시점에 한 명만 락을 획득하고 나머지는 조용히 대기 → 락 해제 후 빈 슬롯이나 새 1번을 본다. **"조회 후 반드시 쓰기까지 가는 흐름"에서는 처음부터 X-lock이 단순하고 안전하다**는 일반 원칙이 우리 케이스에 그대로 적용된다.

## Consequences

긍정:
- promote 도중 대상 waiting의 외부 변경(특히 같은 W1을 대상으로 한 사용자 취소 / 또 다른 promote 시도)이 직렬화된다.
- 동시 cancel/remove로 인한 `reservation` UNIQUE 충돌이 락 단계에서 자연스럽게 해결된다 (한쪽이 W1을 점유하면 다른 쪽은 대기 후 빈 슬롯을 보거나 W2를 본다).
- 결과 상태가 위 (a) / (b) / (c) 셋으로 명확히 좁혀져, 도메인 코드가 "어중간한 상태" 처리를 고려할 필요가 없다.
- **일반 조회는 영향받지 않는다.** H2는 기본 MVCC 모드에서 일반 `SELECT`가 락을 보지 않고 스냅샷을 읽으므로, promote 트랜잭션이 X-lock을 보유한 시간 동안에도 `findReservations` / `findAllByUserName` 같은 화면 조회는 블록 없이 통과한다. 락의 영향은 같은 row에 다시 락을 잡으려는 작업(다른 promote, 같은 row 변경)에만 미친다.

부정/주의:
- **락 자체가 새로운 실패 양상을 만든다.** 락 타임아웃(`JdbcSQLTimeoutException`)이나 데드락 감지로 한쪽 트랜잭션이 롤백될 수 있다. 락이 없을 때 발생하지 않던 종류의 예외가 생긴다.
- **gap lock이 없다.** H2의 `SELECT ... FOR UPDATE`는 row-lock이지 MySQL InnoDB의 Next-Key Lock 같은 gap lock이 아니다. 따라서 "promote 도중 같은 슬롯에 새 waiting이 INSERT 되는 것"은 막지 못한다. 단, 새 waiting은 다음 promote의 대상이 될 뿐이라 의미상 문제는 아니다.
- **JOIN과의 상호작용 — 락의 종류와 락의 범위는 독립 축이다.** `FOR UPDATE` / `FOR SHARE` 키워드는 락의 *강도*만 결정하고, 락이 *어느 row에* 걸리는지는 SQL 구조(JOIN 포함 여부, `OF` 절)가 결정한다. 따라서 키워드를 `FOR SHARE`로 바꿔도 JOIN된 `reservation_time` / `theme` row까지 락 대상이 되는 사실은 바뀌지 않는다. 다른 트랜잭션의 같은 time/theme 행 *변경*이 영향을 받을 수 있다(단, 일반 조회는 위 긍정 항목대로 영향 없음). 좁히려면 `FOR UPDATE OF w`로 락 대상을 명시하거나 JOIN을 분리한 별도 쿼리로 `waiting` row만 락을 잡고 도메인 정보는 락 없는 일반 SELECT로 가져온다.
- **사용자 경험 정책은 별도 영역으로 남는다.** 락은 race를 직렬화할 뿐, "B 사용자가 취소를 요청했는데 그 사이 자기 대기가 승격된 경우 어떤 응답을 줄지"(침묵형 vs 명시형)는 정책 결정이다. 본 ADR에서는 결정하지 않으며, 필요 시 별도 의사결정으로 다룬다.