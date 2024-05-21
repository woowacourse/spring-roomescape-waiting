## 요구사항 명세

- [x] localhost:8080/admin 요청 시 index.html 페이지가 응답한다.
- [x] /admin/reservation 요청 시 reservation-legacy.html 응답한다.
- [x] /reservations 요청 시 예약 목록을 응답한다.
- [x] 예약 페이지 요청 시 예약 목록을 조회하여 보여준다.
- [x] 예약을 추가한다.
- [x] 예약을 삭제한다.


- [x] 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때
    - [x] 시작 시간은 빈칸일 수 없다.
    - [x] HH:mm 형식이어야 한다.

- [x] 예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력 되었을 때
    - [x] 예약자명은 빈칸일 수 없다.
    - [x] 날짜는 빈칸일 수 없다.
    - [x] 날짜는 YYYY-MM-dd 형식이어야 한다.
    - [x] 시간에 대한 아이디는 정수여야 한다.

- [x] 특정 시간에 대한 예약이 존재할 때, 그 시간을 삭제할 수 없다.
- [x] 존재하는 시간에 대해서만 예약할 수 있다.

- [x] 지나간 날짜와 시간에 대한 예약 생성은 불가능하다.
- [x] 중복 예약은 불가능하다. (ex. 이미 4월 1일 10시에 예약이 되어있다면, 4월 1일 10시에 대한 예약을 생성할 수 없다)
- [x] 시간은 중복될 수 없다.

- [x] 모든 테마는 시작 시간이 동일하다.
- [x] 테마 이름은 빈칸일 수 없다.
- [x] 관리자가 테마를 관리할 수 있다.
- [x] 이미 예약 중인 테마는 삭제할 수 없다.
- [x] 존재하는 테마에 대해서만 예약할 수 있다.
- [x] 관리자가 방탈출 예약 시, 테마 정보를 포함할 수 있다.

- [x] 사용자가 날짜를 선택하면 테마를 조회할 수 있다.
- [x] 사용자가 테마를 선택하면 예약 가능한 시간을 조회할 수 있다.
- [x] 사용자는 예약자명을 입력할 수 있다.
- [x] 사용자가 예약할 수 있다.
- [x] 인기 테마 조회 기능을 추가합니다.
    - [x] 최근 일주일을 기준으로 해당 기간 내에 방문하는 예약이 많은 테마 10개를 조회한다.

---

# 방탈출 예약 대기

## 3단계 요구 사항

- [ ] 예약 대기 관련 API(`/reservations/waitlists`)
    - [x] 예약 대기 생성 API
        - [x] request
          ```json
          {
            "date" : "2024-05-20",
            "timeId" : 1,
            "themeId" : 1
          }
          ```
        - [x] response(`201` CREATED)
          ```json
          {
            "id": 1,
            "date": "2024-05-20",
            "member": {
                "id": 1,
                "name": "사용자1"
            },
            "time": {
                "id": 1,
                "startAt": "10:00"
            },
            "theme": {
                "id": 1,
                "name": "방탈출1",
                "description": "1번 방탈출",
                "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
            }
          }
          ```
    - [ ] 예약 대기 취소 API
- [ ] 내 예약 목록 조회 시 예약 대기 목록도 함께 포함
    - [ ] **(심화)** 내 예약 목록의 예약 대기 상태에 몇 번째 대기인지도 함께 표시
        - [ ] 예약 대기 목록에서 date, time_id, theme_id가 동일한 것들을 id 순서대로 정렬
- [ ] 중복 예약이 불가능 하도록 구현
    - [ ] 예약 목록 및 예약 대기 목록에 있는지 확인(date, member_id, time_id, theme_id 모두 동일하면 중복)

### 예약 대기 테이블 스키마

```sql
CREATE TABLE reservation_wait
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    date      DATE   NOT NULL,
    member_id BIGINT NOT NULL,
    time_id   BIGINT NOT NULL,
    theme_id  BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
```
