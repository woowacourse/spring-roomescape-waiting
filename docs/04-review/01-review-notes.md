# 예약 대기 기능 - 리뷰어께 드리는 질문

이번 사이클에서 **"이미 예약된 슬롯에 대기 신청 → 앞 예약 취소 시 다음 대기자 자동 승격"** 기능을 구현했습니다.
리뷰하면서 아래 3가지에 대한 의견을 주시면 정말 감사하겠습니다.

> 구조 요약: `reservation` 한 테이블에서 예약과 대기를 `status`(PENDING/CONFIRMED/COMPLETED/CANCELLED)로 함께 관리하고, 날짜+시간+테마는 `theme_slot` 테이블로 분리해 FK로 참조합니다.

---

## 1. 동시성 - 같은 슬롯에 동시 예약이 들어오면?

예약 생성 시, 슬롯에 기존 예약이 없으면 `CONFIRMED`, 있으면 `PENDING`으로 저장합니다.

```java
if (!reservationRepository.existsByThemeSlotId(themeSlotId)) {
    // 슬롯을 '예약됨'으로 바꾸고, 이 예약을 CONFIRMED로
}
```

빈 슬롯에 두 요청이 거의 동시에 들어오면, 둘 다 `existsByThemeSlotId == false`를 읽어 **둘 다 CONFIRMED로 저장될 수 있다**고 생각합니다. 현재는 락이나 DB 제약을 걸지 않았습니다.

**질문**
- 이런 동시성 문제를 막아야 한다면, DB 제약 / 비관적 락 / 낙관적 락 중 어떤 걸 먼저 고려하시나요?
- 미션 규모에서 동시성까지 방어하는 게 적절한지, 아니면 과한 설계인지도 궁금합니다.

---

## 2. `theme_slot.is_reserved` - 예약 여부를 두 곳에서 관리하는 게 맞을까요?

"이 슬롯이 예약됐는지"를 `theme_slot.is_reserved` 컬럼으로도, `reservation.status`로도 알 수 있습니다. 그래서 취소/승격할 때마다 둘을 같이 맞춰주는 코드가 여기저기 흩어져 있습니다.

`reservation` 테이블만으로(`status = 'CONFIRMED'`) 판단하면 `is_reserved`는 없어도 될 것 같다는 생각이 듭니다.

**질문**
- 이렇게 계산 가능한 값을 별도 컬럼으로 중복해서 들고 있는 게 좋은 선택일까요?
- 둘 중 하나만 남긴다면 어느 쪽을 택하시겠어요?

---

## 3. 테스트 코드 - Fake 객체로 서비스를 테스트하는 방식

서비스 테스트에서 Mockito 대신 Repository 인터페이스를 직접 구현한 **Fake 객체**(`FakeReservationDao`, `FakeThemeSlotDao`)를 사용했습니다.

```java
reservationService = new ReservationService(
        new FakeReservationDao(),
        new FakeThemeSlotDao()
);
```

가짜 구현이라 실제 JDBC 구현과 **동작이 달라질 위험**이 있다고 느꼈습니다. (예: 실제 SQL은 `CANCELLED` 제외 조건이 있는데 Fake에는 빠질 수 있음)

**질문**
- 서비스 테스트에서 Fake 객체와 Mock(Mockito) 중 어떤 걸 선호하시나요?
- Fake 구현이 실제 구현과 어긋나지 않게 하려면 어떤 방법을 쓰시는지 궁금합니다.

---

리뷰 시간 내주셔서 감사합니다!
