# Time Handling Convention

현재 시각이 필요한 서비스 코드는 `LocalDateTime.now()`를 직접 호출하지 않는다.

## 원칙

- 운영 코드는 `Clock`을 생성자 주입으로 받는다.
- 현재 시각은 `LocalDateTime.now(clock)`으로 계산한다.
- 도메인 객체는 현재 시각을 직접 조회하지 않고, 메서드 인자로 전달받는다.
- 테스트에서는 `Clock.fixed(...)`를 사용해 경계값을 검증한다.

## 이유

`LocalDateTime.now()`를 직접 호출하면 테스트 시점에 따라 결과가 달라질 수 있다.
특히 현재 시각 직전/직후 예약 같은 경계값을 안정적으로 검증하기 어렵다.

## 예시

```java
public class ReservationCommandService {

    private final Clock clock;

    public ReservationCommandService(Clock clock) {
        this.clock = clock;
    }

    public Reservation save(...) {
        return Reservation.createWith(
            ...,
            LocalDateTime.now(clock)
        );
    }
}
```

## 테스트 예시

```java
private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-08-05T01:00:00Z"),
        ZoneId.of("Asia/Seoul")
);

private static final LocalDate TODAY = LocalDate.now(FIXED_CLOCK);

@Test
void save는_현재_시각_직전_예약이면_예외를_던진다() {
    ReservationTime pastTime = new ReservationTime(1L, LocalTime.of(9, 59));

    assertThatThrownBy(() -> reservationCommandService.save(
            new ReservationRequest("민욱", TODAY, pastTime.getId(), themeId)
    )).isInstanceOf(BusinessRuleViolationException.class);
}

@Test
void save는_현재_시각과_같은_예약을_과거로_판단하지_않는다() {
    ReservationTime currentTime = new ReservationTime(1L, LocalTime.of(10, 0));

    reservationCommandService.save(
            new ReservationRequest("민욱", TODAY, currentTime.getId(), themeId)
    );
}
```

테스트 날짜는 `LocalDate.of(2999, 1, 1)`처럼 먼 미래의 임의 날짜보다
고정된 `Clock` 기준으로 `LocalDate.now(FIXED_CLOCK).plusDays(1)`처럼 표현한다.
