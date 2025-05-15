# 방탈출 사용자 예약

## 예외 처리

- 예외 처리 후 적절한 응답 반환 (404, 403.. 등)
- 예외 처리
    - 회원
        - 이름 : not null, not blank
        - 이메일 : not null, not blank
        - 비밀번호 : not null, not blank

## 신규 기능

- 데이터 베이스 저장 방식을 JPA 을 전환한다.
- 회원은 생성한 예약을 확인할 수 있다.

## Api

- 회원 예약 조회
    - `/reservations-mine` 요청하면 `/reservations-mine.html` 반환

- API 명세
    - 회원 예약 조회
        - request
      ```
      GET /reservations-mine HTTP/1.1
      cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
      host: localhost:8080
      ```
      response
      ```
      HTTP/1.1 200 
      Content-Type: application/json
      
      [
       {
        "reservationId": 1,
        "theme": "테마1",
        "date": "2024-03-01",
        "time": "10:00",
        "status": "예약"
       },
       {
        "reservationId": 2,
        "theme": "테마2",
        "date": "2024-03-01",
        "time": "12:00",
        "status": "예약"
       },
       {
        "reservationId": 3,
        "theme": "테마3",
        "date": "2024-03-01",
        "time": "14:00",
        "status": "예약"
       }
      ]
      ```
