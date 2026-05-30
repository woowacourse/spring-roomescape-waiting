# 🛠️ 수정한 부분

- ### 계층간 데이터 전달 일관화

> 현재의 구조에 부합한 실용적 예외로  
> Command DTO 도입 대신 Request 의 서비스 계층 침투를 선택했습니다.

## 💬 리뷰 정리

### Review 01

> ### `Booking`(`Reservation` + `Waiting`) 조회 시 `Waiting` 개수만큼 추가 쿼리 발생
>
> Reservation을 조회하고 존재하는 Waiting마다 쿼리가 추가로 발생하게 되어서 매번 2번씩 추가쿼리가 발생하게 됩니다.  
> waiting을 처음에 조회해올 때 Join문을 써서 한번에 가져오면 어떤가요?

### Feedback 01

```java
public class Waiting {

    private final Long id;
    private String name;
    private LocalDate date;
    private TimeSlot timeSlot;
    private Theme theme;
```

```sql
SELECT w.id             AS waiting_id,
       w.name,
       w.date,
       w.waiting_number,
       t.id             AS t_id,
       t.start_at,
       th.id            AS theme_id,
       th.name          AS theme_name,
       th.description   AS theme_description,
       th.thumbnail_url AS theme_thumbnail_url
FROM (SELECT id,
             name, date, time_id, theme_id, ROW_NUMBER() OVER (
          PARTITION BY date, time_id, theme_id
          ORDER BY created_at ASC, id ASC
          ) AS waiting_number
      FROM waiting) w
         INNER JOIN time_slot t ON w.time_id = t.id
         INNER JOIN theme th ON w.theme_id = th.id
WHERE w.id = ?
```

`Waiting` 도 `Reservation` 처럼 객체 기반 참조로 변경하고  
`WaitingRepository` 도 조인을 통해 단일 쿼리로 조회하도록 수정했습니다!

---

### Review 02

> ### 존재하지 않는 예약에 대한 대기 가능
>
> 예약이 있는 슬롯에만 대기를 신청할 수 있도록 하는 조건도 검증해줘야하지 않을까요?

### Feedback 02

```java
    private void validReservation(Waiting waiting) {
    Reservation reservation = findReservationOrThrow(waiting);
    validateOwnership(reservation, waiting);
}

private Reservation findReservationOrThrow(Waiting waiting) {
    return reservationRepository.findByDateAndTimeIdAndThemeId(
            waiting.getDate(),
            waiting.getTimeSlot().getId(),
            waiting.getTheme().getId()
    ).orElseThrow(InvalidWaitingPrerequisiteException::new);
}
```

해당 정보의 예약이 존재하는지 먼저 확인하고, 있을 경우에만 검증을 진행하도록 수정했습니다!

---

### Review 03

> ### 예약의 날짜/시간에 대한 검증
>
> validNotPast가 기존 예약 날짜를 기준으로 검증하고 있는데, reschedule 이후의 날짜로 검사해야하지 않나요?

### Feedback 03

`validNotPast` / `validDateTime` 의 이름이 혼동을 주기 쉬운 것 같네요!  
`validNotPast` 는 해당 예약이 조작 가능한 현재 ~ 미래의 예약인지를 검증하고  
`validDateTime` 은 예약하려는 날짜/시간이 현재 ~ 미래인지를 검증하고 있습니다.

```java

@Transactional
public void patchReservation(long id, String userName, ReservationPatchRequest request) {
    Reservation reservation = validModifiable(id, userName);
    reservation.reschedule(
            request.name(),
            request.date(),
            findOptionalTime(request.timeId()),
            findOptionalTheme(request.themeId())
    );
    validDateTime(request.date(), reservation.getTimeSlot().getStartAt());
    validDuplicatedReservation(reservation);
    reservationRepository.update(reservation);
}

private void validUpcoming(Reservation reservation) {
    LocalDate date = reservation.getDate();
    LocalTime startTime = reservation.getTimeSlot().getStartAt();
    if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now()))) {
        throw new PastReservationControlException();
    }
}

private void validDateTime(LocalDate date, LocalTime time) {
    if (date.isBefore(LocalDate.now())) {
        throw new PastTimeException("지난 날짜로 예약하실 수 없습니다.");
    }
    if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
        throw new PastTimeException("지난 시간으로 예약하실 수 없습니다.");
    }
}
```

`validNotPast` -> `validUpcoming` 로 수정하고 누락된 검증을 추가했습니다!

---

### Review 0

>

### Feedback 0

---

### Review 0

>

### Feedback 0

---

### Review 0

>

### Feedback 0

---

### Review 0

>

### Feedback 0

---

### Review 0

>

### Feedback 0
