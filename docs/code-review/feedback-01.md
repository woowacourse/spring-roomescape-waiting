# 🛠️ 수정한 부분

- ### 계층간 데이터 전달 일관화

> 현재의 구조에 부합한 실용적 예외로  
> Command DTO 도입 대신 Request 의 서비스 계층 침투를 선택했습니다.

## 💬 리뷰 정리

### Review 01

> `Booking`(`Reservation` + `Waiting`) 조회 시 `Waiting` 개수만큼 추가 쿼리 발생

### Reply 01

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

>

### Reply 02

---

### Review 0

>

### Reply 0

---

### Review 0

>

### Reply 0

---

### Review 0

>

### Reply 0

---

### Review 0

>

### Reply 0

---

### Review 0

>

### Reply 0

---

### Review 0

>

### Reply 0
