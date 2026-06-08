# 0002. 예약 대기 승격 로직은 호출자의 트랜잭션 안에서 실행한다

- 상태: Accepted
- 날짜: 2026-06-03
- 관련 코드: `ReservationService.promoteFirstWaiting`, `ReservationService.cancelReservation` / `removeReservation` / `changeReservationSlot`, `WaitingDao.findFirstBySlot`

## Context

Step2에서 "확정 예약 취소 시 같은 슬롯의 1번 대기자가 자동으로 예약으로 승격된다" 정책이 추가되었다(README §4 — 사용자 취소 / 관리자 강제 삭제 / 예약 변경 세 시점에서 트리거).

승격은 두 DAO 작업으로 구성된다.

```
reservationDao.save(new Reservation(...));   // 1번 대기자를 예약으로 INSERT
waitingDao.delete(waiting.getId());          // 해당 waiting 행 삭제
```

이 둘은 **원자적으로** 처리되어야 한다. 하나만 성공하면 같은 사용자가 "예약 + 대기"에 동시에 존재하거나, 슬롯이 비어 다른 사용자가 끼어들 수 있다.

세 트리거 메서드(`cancelReservation`, `removeReservation`, `changeReservationSlot`)에서 같은 흐름이 반복되므로 `promoteFirstWaiting(date, time, theme)` 메서드로 추출이 자연스럽다.

이 시점에 떠오른 질문: **추출한 메서드(private)에도 `@Transactional`을 붙여야 하는가? 원자성은 어디서 보장되는가?**

## Decision

`promoteFirstWaiting`은 **private 메서드로 두고 `@Transactional`을 붙이지 않는다.** 트랜잭션 경계는 호출하는 public 메서드(`cancelReservation` 등) 한 곳에만 선언한다.

```java
@Transactional
public void cancelReservation(Long id, String userName) {
    Reservation origin = getReservationOrThrow(id);
    origin.validateOwner(userName);
    validatePastTime(origin.getDate(), origin.getTime());
    reservationDao.delete(id);
    promoteFirstWaiting(origin.getDate(), origin.getTime(), origin.getTheme());
}

private void promoteFirstWaiting(LocalDate date, ReservationTime time, Theme theme) {
    waitingDao.findFirstBySlot(date, time.getId(), theme.getId())
            .ifPresent(waiting -> {
                reservationDao.save(new Reservation(waiting.getName(), date, time, theme));
                waitingDao.delete(waiting.getId());
            });
}
```

## Rationale

Spring `@Transactional`은 AOP 프록시 기반이라 **프록시를 통과하는 시점**에만 평가된다. 같은 인스턴스 내부의 private 호출이나 self-invocation은 프록시를 거치지 않으므로, private 메서드에 `@Transactional`을 붙여도 런타임에 **무시된다**.

대신 트랜잭션의 실제 공유 메커니즘은 propagation이 아니라 `TransactionSynchronizationManager`의 ThreadLocal이다.

1. 프록시가 트랜잭션을 시작하면 해당 `Connection`을 ThreadLocal(키: `DataSource`)에 박아둔다.
2. 같은 스레드에서 이후 실행되는 `JdbcTemplate` 호출은 `DataSourceUtils.getConnection(dataSource)`로 connection을 얻는다.
3. `DataSourceUtils`는 ThreadLocal에 묶인 connection이 있으면 **새로 만들지 않고 재사용**한다.
4. 결과: 같은 스레드의 모든 JDBC 호출이 같은 connection → 같은 트랜잭션에 자동 참여.

즉 `cancelReservation`이 트랜잭션을 열면, 그 안에서 호출되는 `reservationDao.delete` / `promoteFirstWaiting` 내부의 `reservationDao.save` / `waitingDao.delete`는 전부 같은 트랜잭션에 묶인다. 메서드 추출은 트랜잭션 관점에서 **완전히 투명**하다 — 인라인으로 펼친 것과 동일하다.

**멘탈 모델**: "**프록시를 통과하는 트랜잭션 메서드 진입**이 없으면 propagation은 평가될 일 자체가 없다. 외부 진입에서 한 번 평가될 뿐, 안에서 어떻게 쪼개 호출하든 추가 평가는 없다."

## Consequences

긍정:
- 호출자 한 곳만 트랜잭션 경계를 가져, 어디서 트랜잭션이 시작되는지가 명확하다.
- 승격의 save + delete가 호출자의 다른 DAO 작업(예: `cancelReservation`의 `reservationDao.delete(id)`)과 **같은 단위로 커밋·롤백**된다. 중간에 어디서 예외가 나도 부분 상태가 남지 않는다.
- 같은 흐름을 세 트리거(`cancel` / `remove` / `change`)에서 재사용하면서도, 트리거마다 다른 트랜잭션 경계가 필요한 경우 호출자 쪽만 조정하면 된다.

부정/주의:
- 만약 향후 승격을 **별도 트랜잭션**(`REQUIRES_NEW`)으로 분리해야 한다면, private 호출로는 불가능하다 — Spring AOP 프록시 제약 때문이다. 그 경우 `promoteFirstWaiting`을 **별도 빈**(예: `WaitingPromotionService`)으로 빼고 public 메서드로 노출해야 propagation이 다시 평가된다.
- 트랜잭션이 ThreadLocal로 묶여 있다는 점에서, 승격 로직을 다른 스레드에서 실행(`@Async`, `ExecutorService.submit`, 새 `Thread`)하면 호출자의 트랜잭션이 따라가지 **않는다**. 현재 구현은 동기 호출이므로 문제 없지만, 비동기로 옮기는 변경이 생기면 트랜잭션 경계를 다시 설계해야 한다.

## Alternatives considered

1. **`promoteFirstWaiting`에도 `@Transactional`을 붙인다.**
   런타임에 무시되어 동작은 같으나, 어노테이션이 효과를 가진 듯한 오해를 유발한다. 코드의 의도와 실제 동작이 어긋나는 셈이라 제외.

2. **`promoteFirstWaiting`을 별도 빈으로 분리하고 `REQUIRES_NEW`로 새 트랜잭션을 연다.**
   승격이 호출자 트랜잭션과 **독립적으로** 커밋/롤백된다. 예컨대 호출자가 롤백되어도 승격은 살아남는다.
   현재 요구사항은 정반대다 — "취소가 실패하면 승격도 일어나면 안 되고, 승격이 실패하면 취소도 되돌려야" 한다. 부분 커밋 가능성을 만들 이유가 없다.

3. **두 DAO 호출을 호출자에 인라인으로 그대로 둔다(메서드 추출 안 함).**
   트랜잭션 안전성은 동일하지만 같은 로직이 세 트리거에 중복된다. 단순한 추출의 이득이 더 크다.