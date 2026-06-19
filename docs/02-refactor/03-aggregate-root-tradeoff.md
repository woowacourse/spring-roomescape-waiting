# Aggregate Root 적용 시 영향 분석

---

## 결론: 두 가지 문제가 있고, "값 직접 저장" 방식이 현실적인 절충안이다

Aggregate Root 설계(ThemeSlot → List\<Reservation\>)를 그대로 적용하면 두 가지 현실적 문제가 생긴다.

1. **Reservation을 직접 조회하는 패턴이 어렵다** (`findByName`, `findAll`, `findById`)
2. **`reservation.getDate()`, `reservation.getTheme()` 등 10곳 이상이 한 번에 깨진다**

이 두 문제를 동시에 해결하는 방법은 **Reservation이 ThemeSlot 객체 참조 대신 `date`, `time`, `theme`을 직접 들고 있게** 하는 것이다.
순환 참조도 끊기고, 기존 접근 방법(`reservation.getDate()`)도 그대로 살아난다.

---

## 문제 1: Reservation 직접 접근 패턴이 무너진다

현재 코드에서 Reservation을 ThemeSlot을 거치지 않고 직접 조회하는 곳이 많다.

```
ReservationRepository.findAll()          → List<Reservation>  (전체 목록 조회)
ReservationRepository.findById()         → Optional<Reservation> (단건 조회)
ReservationRepository.findByName()       → List<Reservation>  (내 예약 조회)
ReservationRepository.findByThemeSlotAndPending() → List<Reservation> (대기 목록)
ReservationRepository.findRecentReservationByThemeSlot() → Optional<Reservation>
```

`JdbcReservationRepository`는 현재 JOIN 쿼리로 ThemeSlot 데이터까지 한 번에 가져온다.

```java
// JdbcReservationRepository.reservationRowMapper
new Reservation(
    rs.getLong("r_id"),
    rs.getString("name"),
    new ThemeSlot(            // ← JOIN 결과로 ThemeSlot을 직접 조립
        rs.getLong("theme_slot_id"),
        new Theme(...),
        rs.getObject("date", LocalDate.class),
        new Time(...),
        rs.getBoolean("is_reserved")
    ),
    toStatus(rs.getString("status"))
)
```

Reservation에서 ThemeSlot 참조를 없애면,
이 JOIN 결과를 담을 곳이 없어진다.
`reservation.getDate()`는 어디서 오는가?

---

## 문제 2: ThemeSlot에 위임하는 10곳 이상이 한 번에 깨진다

현재 코드에서 `reservation.getDate()`, `reservation.getTheme()`, `reservation.getTime()`, `reservation.getThemeSlot()` 을 사용하는 곳:

| 파일 | 사용 위치 | 접근 방식 |
|---|---|---|
| `ReservationController` | `toResponse()` | `reservation.getDate()` / `getTime()` / `getTheme()` |
| `ReservationResponse` | `from(reservation)` | 동일 3개 |
| `WaitingReservationResponse` | `from(order, reservation)` | 동일 3개 |
| `ReservationService` | `removeReservation()` | `reservation.getTheme()` / `getDate()` / `getTime()` |
| `ReservationService` | `cancelReservation()` | `reservation.getThemeSlot().getId()` / `getTheme()` / `getDate()` / `getTime()` |
| `ReservationService` | `modifyReservation()` | `reservation.getThemeSlot().getId()` |
| `ReservationService` | `findReservationBy()` | `reservation.getThemeSlot().getId()` |
| `JdbcReservationRepository` | `save()` | `reservation.getThemeSlot().getId()` |
| `JdbcReservationRepository` | `updateThemeSlot()` | `reservation.getThemeSlot().getId()` |

이 중 `reservation.getDate()`, `getTime()`, `getTheme()`은 현재 ThemeSlot에 위임한다.

```java
// Reservation.java 현재
public LocalDate getDate()  { return themeSlot.getDate(); }
public Time getTime()       { return themeSlot.getTime(); }
public Theme getTheme()     { return themeSlot.getTheme(); }
```

ThemeSlot 참조를 끊으면 이 위임이 불가능해지고, 위 표의 모든 곳이 한꺼번에 컴파일 에러가 된다.

---

## 설계 선택지 비교

### Option A — 값 직접 저장 (현실적 권장)

Reservation이 ThemeSlot 객체 대신 필요한 **값만 직접** 들고 있는다.
ThemeSlot을 향한 참조는 객체가 아닌 **ID**만 유지한다.

```java
public class Reservation {
    private final Long id;
    private final String name;
    private final Long themeSlotId;  // 객체 참조 → ID 참조
    private final LocalDate date;    // ThemeSlot에서 복사
    private final Time time;         // ThemeSlot에서 복사
    private final Theme theme;       // ThemeSlot에서 복사
    private ReservationStatus reservationStatus;

    // 기존 위임 메서드가 그대로 살아남는다
    public LocalDate getDate()  { return date; }
    public Time getTime()       { return time; }
    public Theme getTheme()     { return theme; }
}
```

ThemeSlot은 Reservation 목록을 들고 있되, 순환 참조가 없다.

```java
public class ThemeSlot {
    private List<Reservation> reservations; // Reservation이 ThemeSlot을 참조하지 않으므로 안전
}
```

| 항목 | 상태 |
|---|---|
| 순환 참조 | 없음 ✓ |
| `reservation.getDate()` 등 | 그대로 동작 ✓ |
| 직접 Reservation 조회 | 가능 ✓ |
| 기존 코드 변경 범위 | 최소 |

---

### Option B — 엄격한 Aggregate Root (이상적이지만 파괴적)

Reservation은 `id`, `name`, `status`만 들고,
날짜/시간/테마는 ThemeSlot을 통해서만 조회한다.

```java
public class Reservation {
    private final Long id;
    private final String name;
    private ReservationStatus status;
    // date, time, theme 없음
}
```

읽기 전용 화면은 별도 DTO 조회로 처리한다.

```java
// 조회용 읽기 모델 (도메인 객체 아님)
public record ReservationView(long id, String name, LocalDate date, ...) {}
```

| 항목 | 상태 |
|---|---|
| 순환 참조 | 없음 ✓ |
| `reservation.getDate()` 등 | 모두 깨짐 ✗ |
| 직접 Reservation 조회 | 별도 DTO 필요 ✗ |
| 기존 코드 변경 범위 | 전면 재작성 수준 |

현재 코드베이스에서 Option B는 Controller DTO, Repository, Service 전체를 뒤엎어야 하므로 비현실적이다.

---

## Option A 적용 시 실제로 바꿔야 하는 부분

### Reservation.java

```java
// Before
public class Reservation {
    private final ThemeSlot themeSlot;
    public LocalDate getDate()  { return themeSlot.getDate(); }
    public Time getTime()       { return themeSlot.getTime(); }
    public Theme getTheme()     { return themeSlot.getTheme(); }
    public ThemeSlot getThemeSlot() { return themeSlot; }
}

// After
public class Reservation {
    private final Long themeSlotId; // ID만 (객체 참조 제거)
    private final LocalDate date;   // 직접 들고 있음
    private final Time time;
    private final Theme theme;

    public LocalDate getDate()  { return date; }   // 위임 대신 직접 반환
    public Time getTime()       { return time; }
    public Theme getTheme()     { return theme; }
    public Long getThemeSlotId() { return themeSlotId; }
}
```

**변경 없는 곳**: `ReservationController`, `ReservationResponse`, `WaitingReservationResponse`
— `reservation.getDate()`, `getTime()`, `getTheme()`이 그대로 동작하기 때문이다.

---

### JdbcReservationRepository.java

`reservationRowMapper`에서 ThemeSlot 객체를 조립하는 대신 값을 직접 Reservation에 넣는다.

```java
// Before
new Reservation(
    rs.getLong("r_id"),
    rs.getString("name"),
    new ThemeSlot(rs.getLong("theme_slot_id"), new Theme(...), date, time, isReserved),
    status
)

// After
new Reservation(
    rs.getLong("r_id"),
    rs.getString("name"),
    rs.getLong("theme_slot_id"),          // ID만 저장
    rs.getObject("date", LocalDate.class), // 직접 값 저장
    new Time(rs.getLong("t_id"), ...),
    new Theme(rs.getLong("theme_id"), ...),
    status
)
```

`save()`, `updateThemeSlot()`에서 `reservation.getThemeSlot().getId()` → `reservation.getThemeSlotId()`로 교체한다.

---

### ReservationService.java

ThemeSlot 객체를 Reservation에서 꺼내 쓰는 코드만 바뀐다.

```java
// Before
themeSlotRepository.update(
    new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false)
);

// After — reservation.getTheme(), getDate(), getTime()은 여전히 동작
themeSlotRepository.update(
    new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false)
);
// → 이 부분은 변경 없음
```

```java
// Before
reservation.getThemeSlot().getId()

// After
reservation.getThemeSlotId()
```

변경 위치: `cancelReservation()`, `modifyReservation()`, `findReservationBy()` 내 `.getThemeSlot().getId()` 3곳.

---

### FakeReservationRepository.java (테스트)

```java
// Before
reservation.getThemeSlot().getId()
reservation.getTheme().getId()
reservation.getTime().getId()

// After
reservation.getThemeSlotId()
reservation.getTheme().getId()  // 그대로
reservation.getTime().getId()   // 그대로
```

---

## 변경 범위 요약

| 파일 | 변경 여부 | 변경 내용 |
|---|---|---|
| `Reservation.java` | **변경** | ThemeSlot 참조 → ID + 값 직접 저장 |
| `ThemeSlot.java` | **변경** | `List<Reservation>` + 비즈니스 메서드 추가 |
| `JdbcReservationRepository.java` | **변경** | rowMapper, save(), updateThemeSlot() |
| `ReservationService.java` | **일부 변경** | `getThemeSlot().getId()` → `getThemeSlotId()` (3곳) |
| `FakeReservationRepository.java` | **일부 변경** | `getThemeSlot().getId()` → `getThemeSlotId()` |
| `ReservationController.java` | **변경 없음** | `getDate()`, `getTime()`, `getTheme()` 그대로 |
| `ReservationResponse.java` | **변경 없음** | 동일 |
| `WaitingReservationResponse.java` | **변경 없음** | 동일 |
