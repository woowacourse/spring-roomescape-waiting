# 결제 연동 4단계 — ReservationService orderId 생성 및 응답 DTO 변경

## 구현 목표

예약 생성 시 서버가 `orderId`를 발급하고 `amount`를 저장한다.  
클라이언트는 응답에서 이 두 값을 받아 Toss 결제창 초기화에 사용한다.

---

## 변경 파일

### `src/main/java/roomescape/domain/ThemeSlot.java`

`addReservation` 시그니처 변경:

```java
// Before
public Reservation addReservation(String name)

// After
public Reservation addReservation(String name, String orderId, Long amount)
```

Reservation 생성자를 `(name, themeSlotId, date, time, theme, orderId, amount)` 형태로 교체.  
orderId/amount는 서비스 계층에서 주입받는다 — ThemeSlot이 직접 생성하지 않는다.

---

### `src/main/java/roomescape/service/ReservationService.java`

`saveReservation()` 내에 orderId 생성 로직 추가:

```java
String orderId = UUID.randomUUID().toString();  // 36자 영숫자 + 하이픈
Long amount = themeSlot.getTheme().getPrice();
Reservation reservation = themeSlot.addReservation(name, orderId, amount);
```

- `orderId`: `UUID.randomUUID().toString()` — 36자, 영숫자와 `-`만 포함 (Toss 허용 범위 내)
- `amount`: 테마의 `price`를 그대로 사용

---

### `src/main/java/roomescape/controller/dto/ReservationResponse.java`

`orderId`, `amount` 필드 추가:

```java
public record ReservationResponse(
        long id, String name, LocalDate date,
        TimeResponse time, ThemeResponse theme,
        String status,
        String orderId,   // 추가: Toss 결제창 초기화용
        Long amount       // 추가: 결제 금액
) { ... }
```

- `from(Reservation)` 팩토리 메서드에도 두 필드 포함.
- GET 엔드포인트 응답에서는 orderId/amount가 null일 수 있다 (대기자 승격 예약 등).

---

### `src/main/java/roomescape/controller/ReservationController.java`

`toResponse()` 중복 제거 — `ReservationResponse.from()` 위임으로 단순화:

```java
private ReservationResponse toResponse(Reservation reservation) {
    return ReservationResponse.from(reservation);
}
```

---

## 수정된 테스트

### `ThemeSlotTest.java`

`addReservation("이름")` → `addReservation("이름", "order-xxx", 10000L)` 로 변경 (6개 호출부).

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| orderId 생성 위치를 ReservationService로 | orderId는 서버 도메인 정책(결제 흐름)의 산물. ThemeSlot의 책임이 아니다. |
| UUID.randomUUID().toString() 사용 | 36자, 영숫자 + `-`만 포함. Toss 허용 형식(6~64자 영숫자/-/_) 충족. |
| ReservationResponse에 orderId/amount 추가 | 클라이언트가 POST /reservations 응답만 보고 바로 Toss 결제창을 초기화할 수 있게 하기 위함. |
