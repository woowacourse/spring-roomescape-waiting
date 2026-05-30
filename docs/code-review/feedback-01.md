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

---

### Review 0

>

### Feedback 0
