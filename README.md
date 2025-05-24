# 방탈출 사용자 예약

## 예외 처리

- 예외 처리 후 적절한 응답 반환 (404, 403.. 등)
- 예외 처리
    - 회원
        - 이름 : not null, not blank
        - 이메일 : not null, not blank
        - 비밀번호 : not null, not blank

- 대기 처리
    - 상위 대기 존재시 승인 불가
    - 예약 존재시 승인 불가
    - 없는 대기 삭제 불가

## 신규 기능

- 예약을 대기할 수 있다.
- 관리자는 예약을 승인 / 거절할 수 있다.

## Api

- 관리자 대기 조회
    - `/admin/waiting` 요청하면 `/admin/waiting.html` 반환

- API 명세
    - 회원 대기 조회
        - request
      ```
      GET /admin/waitings HTTP/1.1
      cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
      host: localhost:8080
      ```
        - response
      ```
      HTTP/1.1 200 
      Content-Type: application/json
      
      [
       {
      "reservationId": 1,
      "theme": "테마1",
      "date": "2024-03-01",
      "time": "10:00",
      "status": "1번째 대기"
       },
       {
      "reservationId": 2,
      "theme": "테마2",
      "date": "2024-03-01",
      "time": "12:00",
      "status": "2번째 대기"
       },
       {
      "reservationId": 3,
      "theme": "테마3",
      "date": "2024-03-01",
      "time": "14:00",
      "status": "3번째 대기"
       }
      ]
      ```
        - 회원 대기 생성
        - request
      ```
      POST /reservations/waitings HTTP/1.1
      cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
      host: localhost:8080
      {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
      }
      ```
        - response
      ```
      HTTP/1.1 200 
      Content-Type: application/json
      
      [
       {
      "reservationId": 1,
      "theme": "테마1",
      "date": "2024-03-01",
      "time": "10:00",
      "status": "1번째 대기"
       },
       {
      "reservationId": 2,
      "theme": "테마2",
      "date": "2024-03-01",
      "time": "12:00",
      "status": "2번째 대기"
       },
       {
      "reservationId": 3,
      "theme": "테마3",
      "date": "2024-03-01",
      "time": "14:00",
      "status": "3번째 대기"
       }
      ]
      ```
        - 회원 대기 거절
            - request
      ```
      DELETE /admin/waitings HTTP/1.1
      cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
      host: localhost:8080
      ```
        - response
      ```
      HTTP/1.1 200 
      Content-Type: application/json
      ```
