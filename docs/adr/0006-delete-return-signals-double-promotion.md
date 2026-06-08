# 0006. 예약 삭제는 반환값으로 처리 여부를 신호하고, 호출 측이 이중 승격을 방지한다

- 상태: Accepted
- 날짜: 2026-06-04
- 관련 코드: `ReservationDao.delete`, `ReservationService.cancelReservation`, `ReservationService.removeReservation`

## Context

ADR-0003에서 promote 흐름에 비관적 락을 도입하고 시나리오 A·B를 동시성 테스트로 검증했다. 그 시점엔 *waiting을 1건만 셋업*한 시나리오만 봤다.

새 race를 발견했다: **waiting이 2건 이상**일 때, 같은 reservation에 대한 동시 `cancel` + `remove`가 *이중 승격*을 일으킨다.

```
초기: R(A), W1(B 먼저), W2(C 나중)

시점     T1 (cancelReservation)              T2 (removeReservation)
────────────────────────────────────────────────────────────────────────────
t0       findById(R) → 봄
t1                                            findById(R) → 봄 (MVCC, T1 commit 전)
t2       delete(R) [X-lock on R]
t3                                            delete(R) [X-lock 대기]
t4       findFirstBySlot FOR UPDATE → W1
t5       save(reservation, B 데이터) → 새 row INSERT
t6       delete(W1)
t7       COMMIT (모든 lock 해제)
t8                                            delete(R) 재개 → 0 rows (이미 없음, 하지만 void라 신호 못 받음)
t9                                            isPast 가드 통과
t10                                           findFirstBySlot FOR UPDATE → W2
                                              (W1은 t6에서 삭제됨, W2가 새 1번)
t11                                           save(reservation, C 데이터)
                                              → 같은 슬롯에 INSERT 시도
                                              ↓
                                              💥 UNIQUE(date, time_id, theme_id) 위반
                                              → DataIntegrityViolationException
t12                                           T2 트랜잭션 ROLLBACK
────────────────────────────────────────────────────────────────────────────
```

ADR-0003의 락은 *waiting row*에 잡힌다. 같은 W1을 두 번 promote하는 것은 막아주지만, **W1이 사라진 후 T2가 *다음 1번*인 W2를 새 promote 대상으로 인식**해 같은 슬롯에 두 번째 INSERT를 시도하는 것은 막지 못한다.

데이터 정합성은 `reservation`의 UNIQUE 제약이 *마지막 방어선*으로 작동해 보존된다. 다만 진 쪽 트랜잭션이 `DataIntegrityViolationException`으로 죽어 사용자(관리자)에게 500/409 응답이 나간다.

### 무엇이 race의 *진짜 원인*인가

표면적으로는 `reservationDao.save`에서 UNIQUE 위반 예외가 터지는 것이 race의 발현이다. 그러나 더 들어가 보면, **`reservationDao.delete`가 `void` 반환이라 호출자가 "내가 실제로 지운 것인지" 알 수 없다는 점**이 본질이다.

T2의 흐름을 다시 보면:
- t8: T2의 `delete(R)`가 실행되지만, R은 이미 T1이 지운 뒤다 → 0 rows affected
- 그러나 `delete`가 `void`라 T2는 자기 작업이 *실제 효과가 있었는지* 모른다 → 그대로 promote 진입
- promote에서 W2를 잡아 INSERT 시도 → UNIQUE 위반

T2가 자기 delete의 결과를 알 수 있다면 — "내가 실제로 지운 게 아니라면 promote도 내 책임이 아니다"라고 판단해 정상 종료할 수 있다.

## Decision

`ReservationDao.delete`의 시그니처를 `void`에서 `boolean`으로 변경하고, **반환값을 "실제로 row를 지웠는가"라는 신호**로 사용한다. 호출 측(`cancelReservation` / `removeReservation`)은 그 신호를 보고 후속 promote 흐름의 진행 여부를 결정한다.

```java
// ReservationDao
public boolean delete(Long id) {
    int affected = jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    return affected > 0;
}

// ReservationService
@Transactional
public void removeReservation(Long id) {
    Reservation origin = getReservationOrThrow(id);
    if (!reservationDao.delete(id)) {
        return;  // 이미 다른 트랜잭션이 처리 → promote도 내 책임 아님
    }
    if (isPast(origin.getDate(), origin.getTime())) return;
    promoteFirstWaiting(origin.getDate(), origin.getTime(), origin.getTheme());
}

@Transactional
public void cancelReservation(Long id, String userName) {
    Reservation origin = getReservationOrThrow(id);
    origin.validateOwner(userName);
    validatePastTime(origin.getDate(), origin.getTime());
    if (!reservationDao.delete(id)) {
        return;
    }
    promoteFirstWaiting(origin.getDate(), origin.getTime(), origin.getTheme());
}
```

## Rationale

### 어떻게 race를 막는가

`delete`의 X-lock이 두 트랜잭션의 delete를 자연 직렬화한다. 늦게 진입한 쪽은 commit 후 재개 시 `0 rows`를 반환받고, 그걸 신호로 promote 진입 자체를 건너뛴다.

```
T1 (먼저 진입)                           T2 (나중)
─────────────────────────────────────────────────────────────
delete(R) → 1 rows → true → promote 진행 → COMMIT
                                          delete(R) 재개 → 0 rows → false → return
                                          ─ promote 시도조차 안 함
─────────────────────────────────────────────────────────────
```

`save` 시점의 UNIQUE 위반이 *발생하지 않음* — race가 표면화될 단계까지 가지 않는다.

### 멱등성을 깨는가?

> "DELETE 결과를 보고 분기하는 게 멱등성에 맞나?"라는 의문이 떠올랐다.

멱등성의 정의는 **"같은 요청을 N번 보내도 *최종 서버 상태*가 같다"**이다. *매 호출의 효과*가 같을 필요는 없다.

- 첫 호출: reservation 삭제 + 1번 대기자 승격
- 두 번째 호출: 아무 일도 안 함 (이미 처리됨)
- 최종 서버 상태는 둘 다 동일

본 결정은 멱등성을 *깨는* 게 아니라 *활용*하는 패턴이다. 두 호출 모두 호출자에게 동일한 200 응답을 주며, 내부에서 promote 수행 여부만 분기한다. 외부 관점에서는 일관된 멱등 동작이다.

## Verification

동시성 테스트(`ConcurrentTest.동시_cancel과_admin_remove시_waiting이_2건이면_이중승격_race가_발생할_수_있다`)로 1000회 반복했다.

| | 적용 전 (`delete` void) | 적용 후 (`delete` boolean + 분기) |
|---|---|---|
| `DataIntegrityViolationException` (이중 승격 race) | 약 957회 | 0회 |
| `NotFoundException` (별개 timing race — T2 BEGIN이 T1 commit 후) | ~43회 | 도메인적으로 자연스러운 응답 |

이중 승격 race가 96% 비율로 발생하던 것이 완전히 사라졌다. 잔여 `NotFoundException`은 본 결정의 영역이 아닌 별개 race이며, "이미 처리된 예약에 대한 시도 → 404"라는 자연스러운 도메인 응답이다.

## Consequences

긍정:
- 이중 승격 race가 발생 자체를 안 함 (락이나 catch 없이 정상 흐름으로 처리).
- `delete`의 반환값이 "내가 책임질 일이 있는가"라는 도메인 신호로 작동해 의도가 코드에 명확히 드러남.
- 호출자(API 클라이언트)는 race 여부와 무관하게 동일한 200 응답을 받음 → 멱등성 유지.

부정/주의:
- `ReservationDao.delete` 시그니처 변경에 따라 기존 호출자(서비스 두 곳, 테스트)의 stub/검증을 함께 수정해야 한다.
- 이 패턴은 "delete 자동 X-lock이 두 트랜잭션을 직렬화한다"는 DB 동작에 의존한다. 다른 RDB로 옮기는 경우 동일하게 동작하는지 검증이 필요하다.
- waiting 2건 미만 상황(시나리오 B)에서는 이 race 자체가 발생하지 않으므로, 이 결정의 효과는 *waiting이 여러 건 쌓인 슬롯*에서 두드러진다.

## Alternatives considered

1. **promote 진입 전 `existsForUpdate`로 reservation 슬롯 락 잡기.**
   ADR-0005의 메서드를 재사용해 promote 진입 전 슬롯이 비어있는지 확인. race 자체는 막을 수 있으나 *새 락 경계를 또 추가*하는 부담. 신호 활용으로 같은 효과를 얻을 수 있다면 락은 redundant.

2. **`DataIntegrityViolationException`을 catch해서 의미 있는 skip 또는 `ConflictException` 변환.**
   `save` 시점의 예외를 잡아 도메인 신호로 해석. 그러나 Spring `@Transactional`은 RuntimeException 발생 시 *rollback-only*로 표시하므로, catch했더라도 트랜잭션이 commit되지 못한다. `@Transactional(noRollbackFor = ...)` 추가 등 부가 처리가 필요해 복잡도가 늘어난다. 예외 기반 흐름 제어라는 점에서도 정상 흐름보다 덜 명확하다.
