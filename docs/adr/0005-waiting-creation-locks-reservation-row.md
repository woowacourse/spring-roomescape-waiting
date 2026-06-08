# 0005. 예약 대기 생성은 reservation row에 비관적 락을 잡는다

- 상태: Accepted
- 날짜: 2026-06-04
- 관련 코드: `ReservationDao.existsForUpdate`, `WaitingService.save`

## Context

`WaitingService.save` 흐름을 다시 들여다보다가 다음 race를 발견했다.

> 예약 대기를 신청하는 과정에서, 예약이 존재하는 것을 확인하고 → 이제 예약 대기를 삽입하기 전에 → 그 사이 예약이 삭제된다면? 게다가 그 예약 삭제 시점엔 대기가 아직 없어 자동 승격(ADR-0002)이 일어나지 않는다면, 예약이 없는 슬롯에 대기만 INSERT 되어 남는다. 그 대기는 영원히 승격될 트리거가 없다.

코드로 풀면:

```java
if (!reservationDao.existsBy(date, theme, time)) {    // ← check
    throw new UnprocessableEntityException(...);
}
// 다른 검증들 …
waitingDao.save(waiting);                              // ← use
```

check와 use 사이에 다른 트랜잭션이 commit할 수 있는 *시간 차*가 존재한다. 이 사이에 같은 슬롯의 예약이 취소되면 dangling waiting이 생긴다.

```
시점     T1 (waitingService.save)                T2 (cancelReservation)
────────────────────────────────────────────────────────────────────────────
t0       existsBy(...) → true (R1 있음)
t1                                                  reservationDao.delete(id)
t2                                                  findFirstBySlot → empty (대기 0건)
t3                                                  promote skip
t4                                                  COMMIT
t5       (다른 검증들 통과)
t6       waitingDao.save(waiting) → INSERT
t7       COMMIT
────────────────────────────────────────────────────────────────────────────
최종: reservation 0건, waiting 1건 — "예약 없는 슬롯에 대기만 떠 있음"
```

이게 왜 심각한가 — 이렇게 남은 대기는:
- 그 슬롯에 새 예약을 만들려는 사람이 `validateNoWaiting`에 막혀 들어올 수 없고,
- 들어올 사람이 없으니 cancel/remove 트리거도 발생하지 않아,
- 본인이 직접 취소하지 않는 한 영구히 1번 대기로 떠 있다.

ADR-0002가 보장하려던 도메인 불변식("대기는 항상 어떤 예약 뒤에 줄을 서 있다")이 깨진다.

## Decision

`ReservationDao`에 락 전용 메서드 `existsForUpdate`를 신설하고, `WaitingService.save`의 사전 검증에서만 이걸 호출한다. 기존 `existsBy`는 락 없는 버전으로 그대로 둔다.

```java
public boolean existsForUpdate(LocalDate date, Theme theme, ReservationTime time) {
    String sql = """
            SELECT 1
            FROM reservation
            WHERE date = ? AND time_id = ? AND theme_id = ?
            FOR UPDATE
            """;
    return !jdbcTemplate.query(sql, (rs, n) -> 1, date, time.getId(), theme.getId())
            .isEmpty();
}
```

`WaitingService.save` 호출 측:

```java
if (!reservationDao.existsForUpdate(command.date(), theme, time)) {
    throw new UnprocessableEntityException("예약이 존재하지 않으면 예약 대기를 생성할 수 없습니다.");
}
```

`ReservationService.validateDuplicate`(기존 호출자)는 락 없는 `existsBy`를 그대로 사용한다.

## Rationale

### 락이 race를 막는 방식

`SELECT ... FOR UPDATE`는 결과 row가 있을 때 그 row에 X-lock을 잡고 트랜잭션이 끝날 때까지 유지한다. waiting 신청 트랜잭션이 reservation row를 락으로 잡고 있는 동안에는 다른 트랜잭션의 `DELETE`가 대기하게 되므로, "check 후 use 전"에 reservation이 사라지는 race window가 닫힌다.

```
경로 a — T1(신청)이 먼저 락 잡는 경우:
시점     T1 (waitingService.save)                T2 (cancelReservation)
────────────────────────────────────────────────────────────────────────────
t0       SELECT FOR UPDATE → R1 row에 X-lock
t1                                                  delete reservation [락 대기]
t2       waitingDao.save → INSERT
t3       COMMIT (락 해제)
t4                                                  delete 재개 → R1 삭제
t5                                                  findFirstBySlot → T1이 INSERT한 W1 발견
t6                                                  promote W1 → reservation으로
t7                                                  COMMIT
────────────────────────────────────────────────────────────────────────────
최종: reservation 1건(승격됨), waiting 0건 ✓


경로 b — T2(취소)가 먼저 락 잡는 경우:
시점     T1 (waitingService.save)                T2 (cancelReservation)
────────────────────────────────────────────────────────────────────────────
t0                                                  delete reservation [X-lock]
t1       SELECT FOR UPDATE [락 대기]
t2                                                  findFirstBySlot → empty → promote skip
                                                    COMMIT
t3       SELECT 재개 → 결과 empty
t4       "예약이 존재하지 않으면 대기 생성 불가" 예외 → rollback
────────────────────────────────────────────────────────────────────────────
최종: reservation 0건, waiting 0건 ✓ (사용자에게 422 응답)
```

두 경로 모두 dangling waiting이 발생하지 않는다.

### 왜 기존 `existsBy`를 락 잡는 버전으로 *교체*하지 않았는가

> **같은 SQL이지만 호출 의도가 다르면 메서드를 분리한다.**

`existsBy`(단순 존재 확인)와 `existsForUpdate`(락 잡는 존재 확인)는 결과 시맨틱이 거의 같아 보여도 호출자에게 거는 부담이 다르다. 같은 이름으로 묶으면 호출자가 시그니처만 보고 "단순 read"라고 가정할 수 있는데 실제로는 X-lock이 잡혀 다른 트랜잭션을 대기시킨다. 의도와 동작이 어긋난다.

또한 `existsBy`의 다른 호출자(`ReservationService.validateDuplicate`)에서는 락이 사실상 redundant이므로 — 어차피 자신의 INSERT가 UNIQUE 제약으로 race를 막아준다 — 그 호출자에게 불필요한 락을 강요할 이유가 없다.

## Verification

이 결정을 검증하기 위해 동시성 테스트(`ConcurrentTest.동시_waiting신청과_예약취소가_충돌해도_dangling_waiting이_없다`)를 작성하고 락 유무를 토글하며 1000회 반복했다.

| 락 상태 | 1000회 중 dangling 발생 횟수 |
|---|---|
| `existsForUpdate`에 `FOR UPDATE` 적용 | 0회 (반복 실행에서 안정적) |
| `FOR UPDATE` 제거 | 수백 회 (실행마다 변동, 대략 500~760회 범위) |

빈도 자체는 OS 스케줄링에 따라 실행마다 변동하지만, 락 없는 상태에서는 1000회 중 절반 이상의 회차에서 dangling이 발생한다는 사실은 안정적으로 재현된다. 본 결정의 락은 실효적이라고 본다.

### 미해결 — 동시 스레드 시작 방식이 TOCTOU race를 재현한다고 말할 수 있는가?

테스트 작성 중 떠오른 의문이다. TOCTOU race는 본래 *한 트랜잭션 안에서 check와 use 사이에 외부 commit이 끼는* 상황을 가리키는데, 우리 테스트는 두 별개 트랜잭션을 `CountDownLatch`로 동시 출발시킨다. 이 두 접근이 정확히 같은 race를 검증하는지에 대해서는 본 ADR 시점에는 결론을 내리지 못했다.

다만 실험 결과로 보면 1000회 중 절반 이상의 회차에서 race가 자연스럽게 발생했고, 이로써 검증 도구로서의 *실용성*은 확인됐다고 본다. 두 방식이 *형식적으로* 같은 race를 다루는지는 추후 학습/검토 영역으로 남긴다.

## Consequences

긍정:
- dangling waiting을 막아 ADR-0002의 도메인 불변식("대기는 항상 어떤 예약 뒤에 줄을 서 있다")이 유지된다.
- 의도가 메서드 이름(`existsForUpdate`)에 드러나 호출자가 락 부담을 인지할 수 있다.
- 다른 호출자(`validateDuplicate`)는 영향받지 않는다.

부정/주의:
- 또 하나의 비관적 락 경계가 늘어, 락 timeout/deadlock 가능성이 미세하게 증가한다.
- `SELECT ... FOR UPDATE`의 정확한 동작(특히 결과 row가 없을 때)은 RDB마다 다르다. 본 결정은 H2 기준이며 다른 RDB로 옮기는 경우 동작 검증이 필요하다.