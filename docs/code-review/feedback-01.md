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

### Review 04

> ### JDBC 환경에서 엔티티의 불변성
> 불변객체로 만들고 새로운 값으로 객체를 새로 만들어서 저장을 해주는 방식이 어떤가요?  
> 만약 객체를 가변상태로 유지하게 된다면 어느 시점에서 객체가 변경됐는지? DB에 저장됐는지도 추적해야하지 않나요?

### Feedback 04

아직 적용하지도 않은 영속성 컨텍스트를 먼저 고려한 가변 설계였네요.  
로직이 가벼우니 도메인 객체를 VO 수준으로 사용하겠다는 의견에 반대해놓곤  
나중에서야 필요할 가변 엔티티를 구현해놓다니 피장파장이네요. 😭  
도메인 객체들을 불변화하고 실행 흐름에 반영했습니다!

추가로 레코드를 애용하는 입장에서 혹시나 불변 도메인 객체에  
레코드를 사용한 경우가 실제로 있는지도 궁금합니다!

---

### Review 05

> ### 중복 조회 로직 제거
> existsAndModifiableReservation 여기 내부에서 이미 findBy를 한번 하는데  
> 이 값을 쓰도록 구조를 수정하면 쿼리를 줄일 수 있을 것 같습니다

### Feedback 05

```java
private Reservation validModifiable(long id, String userName) {
    Reservation existingReservation = findReservationById(id);
    existingReservation.validateModifiable(userName);
    validUpcoming(existingReservation);
    return existingReservation;
}
```

[Feedback03](https://github.com/woowacourse/spring-roomescape-waiting/pull/413#discussion_r3320869563) 반영 과정에서 존재 여부와 조작
가능 여부를 동시에 검증하고 존재하는 예약을 리턴하도록 수정했습니다!

---

### Review 06

> ### UPDATE 쿼리의 결과값 반환
> 여기 조회하는 부분도 update 했을 때 객체를 반환하도록 수정하면 불필요한 조회를 줄일 수 있겠네요

### Feedback 06

앞선 미션에서
[CQS 에 대해 학습한 내용](https://github.com/nn98/spring-roomescape-admin/blob/nn98/docs/study-log/log-02.md) 과
설정한 [실용적 예외의 기준](https://github.com/woowacourse/spring-roomescape-member/pull/409#discussion_r3209591855) 에 따라  
생성에서의 결과 반환은 구현했는데 수정도 동일하게 처리하는 것이 좋겠네요.  
각 실행 흐름에서 수정된 결과 객체를 반환하도록 수정했습니다!

---

### Review 07

> ### SimpleJdbcInsert 중복 생성
> 이렇게 작성하면 매 호출마다 SimpleJdbcInsert를 생성하게 됩니다.  
> 해당 객체는 메타 데이터만 조회하는 것이라서 한번만 초기화하고 사용하시면 됩니다.

### Feedback 07

잘못된 사용법이었네요. 생성자에서 초기화 후 재사용하는 방식으로 수정했습니다!

---

### Review 08

> ### 불필요한 조회 로직 반복
> 이미 saveWaiting 흐름 안에서 validReservation 에서 한번 timeSlot을 조회했는데 또 한번 조회하고 있습니다.  
> 한번만 조회하고 해당 객체를 전달해주면 DB 조회를 줄일 수 있을 것 같네요

### Feedback 08

`Reservation` 조회에 `TimeSlot` / `Theme` 가 다 조인된다는 점을 감안하지 못했네요!

```java

@Transactional
public Waiting saveWaiting(WaitingRequest request) {
    Reservation reservation = findReservationOrThrow(request.date(), request.timeId(), request.themeId());
    validNotReservedBySelf(reservation, request.name());
    Waiting waiting = Waiting.transientOf(request.name(), request.date(), reservation.getTimeSlot(), reservation.getTheme());

    validDuplicated(waiting);
    validDateTime(waiting.getDate(), waiting.getTimeSlot().getStartAt());

    return waitingRepository.save(waiting);
}
```

식별자 참조 -> 객체 참조로 변경된 구조에 맞춰  
`WaitingRequest` 값을 기반으로 `Reservation` 을 조회하고 재활용했습니다!

---

### Review 09

> ### 대기 순번 조회 방식 혼재
> 현재 대기 순번을 조회하는 방식이 2가지가 혼재되어 있는 것 같습니다.  
> 하나의 로직으로 통일하는게 어떤가요?

### Feedback 09

[이전 리뷰](https://github.com/woowacourse/spring-roomescape-waiting/pull/413#discussion_r3320846648) 반영 과정에서 일원화했습니다!  
쿼리를 상수로 분리하는건 별로 선호하지 않지만 쿼리가 길어져서 확인이 힘들 때나  
공통적으로 쿼리에 등장하는 부분이 있을 때는 상수 추출이 꽤나 효과적이네요.

---

### Review 11

> ### 본말전도
> 단어에 너무 매몰될 필요는 없어보입니다.  
> 단순하게 해야할 일에만 우선 집중을 해봅시다.  
> 정답이 있는 문제는 아니기 때문에 타당한 근거만 뒷받침된다면 그렇게 선택하셔도 됩니다.

### Feedback 11

언제나처럼 이론은 이론대로 알되 진짜 중요한, 핵심 목표와 근본을 상기할 부분이었네요.  
원칙이란 것도 결국 실무에서 나온 산출물이니까요.  
많이 후련해지고 잘 다잡을 수 있게끔 깨달음을 주셔서 감사합니다! 
