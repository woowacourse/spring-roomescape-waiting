# 리뷰어 피드백 분석

## 2. 예약 생성·대기·취소 플로우 / 3. Domain 객체지향 구현

두 피드백은 같은 문제를 다른 각도에서 바라본 것이다.

---

## 현재 구조의 문제: 서비스가 두 객체를 동시에 직접 조율하고 있다

`ReservationService.saveReservation`을 보면:

```java
// 서비스가 ThemeSlot 상태도 직접 건드리고, Reservation 상태도 직접 건드림
themeSlot.swtichIsReserved();           // ThemeSlot 상태 변경
themeSlotRepository.update(themeSlot);  // ThemeSlot 저장
reservation.confirm();                   // Reservation 상태 변경
reservationRepository.save(reservation); // Reservation 저장
```

`cancelReservation`도 마찬가지:

```java
reservation.cancel();
reservationRepository.updateStatus(reservation);

waitingReservation.ifPresent(Reservation::confirm); // 다음 대기자 확정
reservationRepository.updateStatus(waitingReservation.get());

themeSlotRepository.update(..., false); // ThemeSlot 상태도 직접 변경
```

서비스가 "다음 대기자 누가 있는지 확인해서 confirm 시키고, ThemeSlot isReserved도 변경해"라는 도메인 지식을 직접 알고 있는 상황이다.
이것이 리뷰어가 말한 **"도메인 규칙이 서비스에 새어나온다"** 는 의미다.

---

## 리뷰어가 제안하는 방향: ThemeSlot이 Reservation들을 소유

현재 DB 구조를 보면 Reservation이 ThemeSlot의 FK를 가진다. 즉 ThemeSlot이 부모다.
그런데 코드 상에서는 ThemeSlot이 자신에게 달린 예약들을 모른다.

리뷰어는 이런 구조를 암시한다:

```java
// ThemeSlot이 예약 목록을 직접 들고 있는 구조
public class ThemeSlot {
    private List<Reservation> reservations;

    public void addReservation(String name) {
        Reservation newReservation = new Reservation(name, this);
        if (reservations.isEmpty()) {
            newReservation.confirm(); // 첫 예약이면 바로 확정
            this.isReserved = true;
        }
        reservations.add(newReservation);
        // 이 규칙이 ThemeSlot 안에 응집됨
    }

    public void cancelReservation(Long reservationId) {
        // 취소 + 다음 대기자 자동 확정 로직이 여기 있음
        // 서비스는 그냥 themeSlot.cancelReservation(id) 만 호출하면 됨
    }
}
```

그러면 서비스는:

```java
// 서비스는 흐름만 제어, 도메인 규칙은 모름
ThemeSlot themeSlot = themeSlotRepository.findWithReservations(themeSlotId);
themeSlot.addReservation(name);
themeSlotRepository.save(themeSlot);
```

---

## 정리

| | 현재 | 리뷰어가 원하는 방향 |
|---|---|---|
| **중심 객체** | Reservation | ThemeSlot |
| **도메인 규칙 위치** | ReservationService | ThemeSlot 내부 |
| **서비스의 역할** | 규칙까지 직접 구현 | 흐름 제어만 |
| **Reservation의 위치** | 독립적인 객체 | ThemeSlot이 소유하는 객체 |

한 문장 요약: **"Reservation은 ThemeSlot 없이 존재할 수 없으니, ThemeSlot이 Reservation들을 직접 관리하게 하고, 예약 확정/취소/대기순번 같은 규칙을 ThemeSlot 안으로 끌어들여라."**

DDD 용어로는 ThemeSlot을 **Aggregate Root**로 만들자는 제안이다.
