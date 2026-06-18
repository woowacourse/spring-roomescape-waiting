# ThemeSlot과 Reservation의 객체지향적 설계

---

## 결론: 순환 참조 문제는 Reservation에서 ThemeSlot 참조를 끊으면 해결된다

리뷰어가 제안한 "ThemeSlot이 List\<Reservation\>을 들고 있는 구조"는 올바른 방향이지만,
Reservation이 여전히 ThemeSlot을 참조하고 있으면 양방향 순환이 생긴다.
핵심은 **Reservation이 ThemeSlot 객체를 참조하지 않도록** 설계를 바꾸는 것이다.

---

## 문제 1: 양방향 순환 참조

### 현재 코드

```java
// Reservation이 ThemeSlot을 들고 있다
public class Reservation {
    private final ThemeSlot themeSlot; // ← ThemeSlot 참조
}

// 리뷰어 제안대로 ThemeSlot이 List<Reservation>을 들면?
public class ThemeSlot {
    private List<Reservation> reservations; // ← Reservation 참조
}
```

이렇게 하면 아래 순환이 생긴다.

```
ThemeSlot → List<Reservation> → Reservation → ThemeSlot → List<Reservation> → ...
```

**실제로 어떤 문제가 터지는가:**

| 시점 | 문제 |
|---|---|
| JSON 직렬화 | Jackson이 무한 루프에 빠져 StackOverflowError |
| `toString()` 호출 | 동일하게 무한 재귀 |
| `hashCode()` / `equals()` | ThemeSlot.hashCode() → Reservation.hashCode() → ThemeSlot.hashCode() → ... |
| 로그 출력 | 객체를 출력하려다 죽음 |

---

## 문제 2: 동기화 불일치

양방향 참조를 유지한다고 해도, **두 쪽을 항상 같이 갱신해야 하는 문제**가 생긴다.

```java
// Reservation 생성 시 — 두 군데를 모두 갱신해야 한다
Reservation reservation = new Reservation("브라운", themeSlot); // Reservation → ThemeSlot 연결
themeSlot.addReservation(reservation);                            // ThemeSlot → Reservation 연결

// 하나라도 빠트리면 두 객체가 서로 다른 상태를 가리킨다
```

한쪽만 갱신하면 `themeSlot.getReservations()`와 `reservation.getThemeSlot()`이 다른 상태를 가리키는 불일치가 발생한다.
이 문제를 코드로 강제할 방법이 없기 때문에 실수가 생길 수밖에 없다.

---

## 해결: Aggregate Root 패턴

### 핵심 아이디어

> Reservation은 ThemeSlot **안에서만** 의미를 가진다.
> Reservation이 자신이 어느 ThemeSlot에 속하는지 알 필요가 없다.
> ThemeSlot이 자신의 Reservation들을 전부 관리한다.

이를 DDD 용어로 **Aggregate Root(ThemeSlot) + 내부 Entity(Reservation)** 라고 부른다.

### 구조 변경

```
[Before]                          [After]
Reservation ──→ ThemeSlot         ThemeSlot ──→ List<Reservation>
ThemeSlot ──→ List<Reservation>   Reservation (ThemeSlot 참조 없음)
(양방향, 동기화 문제)               (단방향, 동기화 불필요)
```

---

## 현재 코드 기준으로 어떻게 달라지는가

### Reservation 변경: ThemeSlot 참조 제거

현재 Reservation은 ThemeSlot을 통해 날짜, 시간, 테마를 위임한다.

```java
// 현재
public LocalDate getDate() { return themeSlot.getDate(); }
public Time getTime()       { return themeSlot.getTime(); }
public Theme getTheme()     { return themeSlot.getTheme(); }
```

ThemeSlot 참조를 끊으면 이 위임이 불가능해진다.
Reservation이 직접 필요한 값을 들고 있어야 한다.

```java
// 변경 후
public class Reservation {
    private final Long id;
    private final String name;
    private ReservationStatus reservationStatus;
    // ThemeSlot 참조 없음 — ThemeSlot이 Reservation을 감싸고 있으므로 역방향 참조 불필요
}
```

Reservation 입장에서 날짜/시간/테마는 자신의 책임이 아니다.
어느 슬롯에 속하는지는 ThemeSlot이 알고 있다.

---

### ThemeSlot 변경: Reservation을 직접 관리

비즈니스 로직이 서비스에서 ThemeSlot 안으로 들어온다.

```java
public class ThemeSlot {
    private final Long id;
    private final Theme theme;
    private final LocalDate date;
    private final Time time;
    private boolean isReserved;
    private final List<Reservation> reservations = new ArrayList<>();

    // 현재 ReservationService.saveReservation에 있던 판단 로직
    public Reservation addReservation(String name) {
        validateDuplicate(name);
        Reservation reservation = new Reservation(name);
        if (hasNoActiveReservation()) {
            reservation.confirm();
            this.isReserved = true;
        }
        reservations.add(reservation);
        return reservation;
    }

    // 현재 ReservationService.cancelReservation에 있던 판단 로직
    public void cancelReservation(Long reservationId) {
        Reservation target = findById(reservationId);
        boolean wasConfirmed = target.isConfirmed();
        target.cancel();

        if (wasConfirmed) {
            reservations.stream()
                    .filter(Reservation::isPending)
                    .min(Comparator.comparing(Reservation::getId))
                    .ifPresentOrElse(
                            Reservation::confirm,
                            () -> this.isReserved = false
                    );
        }
    }

    private boolean hasNoActiveReservation() {
        return reservations.stream().noneMatch(r -> !r.isCancelled());
    }

    private void validateDuplicate(String name) {
        boolean exists = reservations.stream()
                .anyMatch(r -> r.getName().equals(name) && !r.isCancelled());
        if (exists) throw new CustomException(RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT);
    }
}
```

동기화 문제가 사라진다.
`reservations` 목록의 유일한 주인이 ThemeSlot이기 때문에,
Reservation을 추가하거나 상태를 바꾸는 모든 행위가 ThemeSlot을 통해서만 일어난다.

---

### Service 변경: 흐름 제어만 남는다

```java
// 현재 ReservationService.saveReservation (도메인 규칙이 서비스에 있음)
if (!reservationRepository.existsByThemeSlotId(themeSlotId)) {
    themeSlot.swtichIsReserved();
    themeSlotRepository.update(themeSlot);
    reservation.confirm();
}
return reservationRepository.save(reservation);

// 변경 후 (서비스는 흐름만 제어)
ThemeSlot themeSlot = themeSlotRepository.findWithReservations(themeSlotId);
themeSlot.addReservation(name);     // 규칙은 ThemeSlot 안에 있다
themeSlotRepository.save(themeSlot); // ThemeSlot + Reservation 함께 저장
```

---

## 변경 전/후 책임 비교

| 관심사 | 변경 전 위치 | 변경 후 위치 |
|---|---|---|
| 첫 예약이면 CONFIRMED | `ReservationService` | `ThemeSlot.addReservation()` |
| 취소 후 대기자 승격 | `ReservationService` | `ThemeSlot.cancelReservation()` |
| 중복 예약 검증 | `ReservationService` | `ThemeSlot.validateDuplicate()` |
| 날짜 조회 | `Reservation.getDate()` → `themeSlot` 위임 | `ThemeSlot.getDate()` 직접 |
| 순환 참조 | 발생 | 없음 (단방향) |
| 동기화 | 수동 관리 필요 | ThemeSlot이 단독 관리 |

---

## 남는 고민: Repository를 어떻게 바꿔야 하는가

이 설계를 실제로 적용하면 Repository도 달라져야 한다.

지금은 Reservation과 ThemeSlot이 각각 별도의 Repository를 가진다.
Aggregate Root 패턴에서는 **ThemeSlot Repository 하나가 ThemeSlot과 그 안의 Reservation을 함께 조회·저장**한다.

```java
// ThemeSlotRepository가 Reservation까지 함께 관리
ThemeSlot findWithReservations(Long themeSlotId); // JOIN으로 Reservation 목록까지 로딩
void save(ThemeSlot themeSlot);                    // ThemeSlot + Reservation 변경사항 함께 저장
```

이렇게 되면 `ReservationRepository`가 필요한지도 재검토 대상이 된다.
Reservation 단독으로 조회할 일이 있다면 별도 Repository를 유지할 수 있지만,
그 경우 Reservation은 단순 조회 전용이고 변경은 ThemeSlot을 통해서만 한다는 규칙이 필요하다.
