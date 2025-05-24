# 📌Backend API Endpoints

- AuthRole(접근 권한) 종류
    - PUBLIC: 모든 클라이언트가 접근 가능한 엔드포인트
    - ADMIN: 관리자 권한의 JWT를 가진 사용자만 접근 가능한 엔드포인트
    - MEMBER: 일반 회원 권한의 JWT를 가진 사용자만 접근 가능한 엔드포인트

## ✅ REST API

### ▶️ 회원가입/탈퇴

| HTTP Method | Endpoint      | AuthRole      | Success        | Description               |
|-------------|---------------|---------------|----------------|---------------------------|
| POST        | /members      | PUBLIC        | 201 CREATED    | 회원 추가                     |
| DELETE      | /members/{id} | ADMIN, MEMBER | 204 NO_CONTENT | id(PK)에 해당하는 회원 삭제        |
| GET         | /members      | ADMIN         | 200 OK         | 모든 회원 목록 조회 (id(PK), 이름만) |

### ▶️ 로그인/로그아웃

| HTTP Method | Endpoint     | AuthRole      | Success          | Description                                                                |
|-------------|--------------|---------------|------------------|----------------------------------------------------------------------------|
| POST        | /login       | PUBLIC        | 200 OK           | 클라이언트의 로그인 요청 처리. 로그인에 성공하면 Set-Cookie 헤더에 token={access-token};을 추가하여 응답. |
| POST        | /logout      | ADMIN, MEMBER | 200 OK           | Set-Cookie 헤더에 token의 만료기한을 0초로 세팅하여 응답 (브라우저에서 token이 삭제되길 기대)            |
| GET         | /login/check | PUBLIC        | 200 OK           | 클라이언트가 로그인 된 상태면 사용자의 이름을 응답으로 반환.                                         |
| GET         | /login/check | PUBLIC        | 401 UNAUTHORIZED | 클라이언트가 로그인되지 않은 상태면 권한 없음 반환.                                              |

### ▶️ 예약 시간

| HTTP Method | Endpoint    | AuthRole | Success        | Description           |
|-------------|-------------|----------|----------------|-----------------------|
| POST        | /times      | ADMIN    | 201 CREATED    | 예약 시간 추가              |
| DELETE      | /times/{id} | ADMIN    | 204 NO_CONTENT | id(PK)에 해당하는 예약 시간 삭제 |
| GET         | /times      | PUBLIC   | 200 OK         | 모든 예약 시간 목록 조회        |

### ▶️ 테마

| HTTP Method | Endpoint             | AuthRole | Success        | Description        |
|-------------|----------------------|----------|----------------|--------------------|
| DELETE      | /themes/{id}         | ADMIN    | 204 NO_CONTENT | id(PK)에 해당하는 테마 삭제 |
| GET         | /themes              | PUBLIC   | 200 OK         | 모든 테마 목록 조회        |
| GET         | /themes/popular-list | PUBLIC   | 200 OK         | 인기 테마 목록 조회        |
| POST        | /themes              | ADMIN    | 201 CREATED    | 테마 추가              |

### ▶️ 예약

- 관리자 예약 기능

| HTTP Method | Endpoint                     | AuthRole | Success        | Description                                    |
|-------------|------------------------------|----------|----------------|------------------------------------------------|
| POST        | /admin/reservations          | ADMIN    | 201 CREATED    | 관리자 권한 예약 추가                                   |
| DELETE      | /admin/reservations/{id}     | ADMIN    | 204 NO_CONTENT | id(PK)에 해당하는 예약 삭제(모든 예약 삭제 가능)                |
| GET         | /admin/reservations          | ADMIN    | 200 OK         | 모든 회원의 전체 예약 목록 조회                             |
| GET         | /admin/reservations/filtered | ADMIN    | 200 OK         | 조건부 예약 목록 조회 (회원 id, 테마 id, 시작 기간, 끝 기간으로 필터링) |
| GET         | /admin/reservations/statuses | ADMIN    | 200 OK         | 예약 상태 목록 조회                                    |

- 회원 예약 기능

| HTTP Method | Endpoint                      | AuthRole      | Success        | Description                       |
|-------------|-------------------------------|---------------|----------------|-----------------------------------|
| POST        | /reservations                 | ADMIN, MEMBER | 201 CREATED    | 회원의 예약 추가                         |
| DELETE      | /reservations/{id}            | ADMIN, MEMBER | 204 NO_CONTENT | id(PK)에 해당하는 예약 삭제(본인의 예약만 삭제 가능) |
| GET         | /reservations/mine            | ADMIN, MEMBER | 200 OK         | 내 예약 목록 조회                        |
| GET         | /reservations/available-times | PUBLIC        | 200 OK         | 조건부 예약 가능한 시간 목록 조회(예약 날짜, 테마 id) |

### ▶️ 예약 대기

- 관리자 예약 기능

| HTTP Method | Endpoint             | AuthRole | Success        | Description            |
|-------------|----------------------|----------|----------------|------------------------|
| POST        | /admin/waitings      | ADMIN    | 201 CREATED    | 관리자 권한으로 예약 대기 추가      |
| DELETE      | /admin/waitings/{id} | ADMIN    | 204 NO_CONTENT | 관리자 권한으로 예약 대기 삭제      |
| GET         | /admin/waitings      | ADMIN    | 200 OK         | 전체 예약 대기 목록 조회 (순위 포함) |

- 회원 예약 기능

| HTTP Method | Endpoint       | AuthRole      | Success        | Description   |
|-------------|----------------|---------------|----------------|---------------|
| POST        | /waitings      | ADMIN, MEMBER | 201 CREATED    | 회원의 예약 대기 추가  |
| DELETE      | /waitings      | ADMIN, MEMBER | 204 NO_CONTENT | 회원의 예약 대기 삭제  |
| GET         | /waitings/mine | ADMIN, MEMBER | 200 OK         | 내 예약 대기 목록 조회 |

## ✅ View API

| HTTP Method | Endpoint           | AuthRole | Success | Description                  |
|-------------|--------------------|----------|---------|------------------------------|
| GET         | /                  | PUBLIC   | 200 OK  | 방탈출 사이트 홈페이지                 |
| GET         | /login             | PUBLIC   | 200 OK  | 로그인 페이지                      |
| GET         | /signup            | PUBLIC   | 200 OK  | 회원가입 페이지                     |
| GET         | /reservation       | PUBLIC   | 200 OK  | 회원이 방탈출 예약을 할 수 있도록 도와주는 페이지 |
| GET         | /reservation-mine  | PUBLIC   | 200 OK  | 회원의 내 예약 목록 확인 페이지           |
| GET         | /waiting-mine      | PUBLIC   | 200 OK  | 회원의 내 예약 대기 목록 확인 페이지        |
| GET         | /admin             | ADMIN    | 200 OK  | 관리자 홈페이지                     |
| GET         | /admin/time        | ADMIN    | 200 OK  | 관리자의 예약 시간 관리 페이지            |
| GET         | /admin/theme       | ADMIN    | 200 OK  | 관리자의 테마 관리 페이지               |
| GET         | /admin/reservation | ADMIN    | 200 OK  | 관리자의 예약 관리 페이지               |
| GET         | /admin/waiting     | ADMIN    | 200 OK  | 관리자의 예약 대기 관리 페이지            |

# 📌비즈니스 로직

## ✅ 관리자 시스템

### ▶️ 예약 시간(ReservationTime)

```java
class ReservationTime {
    Long id; // PK
    LocalTime time; // 예약 시간
}
```

- 예약 시간 추가
    - 검증
        - [x] 예약 시간은 10:00~22:00이어야 한다.
        - [x] 예약 시간은 중복될 수 없다.

- 예약 시간 삭제
    - 검증
        - [x] 해당 예약 시간으로 등록된 예약이 있으면 삭제할 수 없다.

### ▶️ 테마(Theme)

```java
class Theme {
    Long id; // PK
    String name; // 테마 이름
    String description; // 테마 설명
    String thumbnail; // 테마 썸네일
}
```

- 테마 추가
    - 검증
        - [x] 테마 이름은 중복될 수 없다.

- 테마 삭제
    - 검증
        - [x] 해당 테마로 등록된 예약이 있으면 삭제할 수 없다.

### ▶️ 예약(Reservation)

```java
class Reservation {
    Long id; // PK
    LocalDate date; // 예약 날짜
    ReservationTime time; // 예약 시간
    Theme theme; // 테마
    Member member; // 예약한 계정
    ReservationStatus status; // 예약 상태: BOOKED, PAID
}
```

- 예약 추가
    - 검증
        - [x] (날짜, 시간, 테마, 예약 완료 상태)가 중복될 수 없다.

- 예약 취소
    - 관리자는 모든 회원의 예약을 취소할 수 있다.
    - 예약 취소 시, 예약 대기가 있으면 우선 순위에 따라 자동으로 예약 자리(날짜, 시간, 테마)가 채워진다.
    - 검증
        - [x] 존재하는 예약만 삭제할 수 있다.

### ▶️ 예약 대기(Waiting)

```java
class Waiting {
    Long id; // PK
    LocalDate date; // 예약 대기 날짜
    ReservationTime time; // 예약 대기 시간
    Theme theme; // 테마
    Member member; // 예약한 계정
    LocalDateTime createdAt; // 예약 대기 요청 시간
}
```

- 예약 대기 추가
    - 검증
        - [x] (날짜, 시간, 테마, 예약 완료 상태)인 예약이 없으면, 예약 대기를 추가할 수 없다.
        - [x] 예약 완료 상태인 본인 예약이 있으면, 예약 대기를 추가할 수 없다
        - [x] 이미 해당 회원이 추가한 예약 대기가 있으면 안된다.

- 예약 대기 삭제

## ✅ 회원 시스템

### ▶️ 회원 계정(Member)

- 회원 계정 추가
    - 검증
        - [x] 회원의 이메일은 중복될 수 없다.

- 회원 계정 삭제
    - 검증
        - [x] 회원은 본인의 계정만 삭제할 수 있다.
        - [x] 단, 관리자는 본인 계정(관리자 계정)을 삭제할 수 없다.

### ▶️ 예약(Reservation)

- 예약 추가
    - 검증
        - [x] (날짜, 시간, 테마, 예약 완료 상태)가 중복될 수 없다.
        - [x] 회원은 과거 시간으로 예약을 추가할 수 없다.

- 예약 삭제
    - 검증
        - [x] 회원은 본인의 예약만 삭제할 수 있다.
        - [x] 존재하는 예약만 삭제할 수 있다.

### ▶️ 예약 대기(Waiting)

- 예약 대기 추가
    - 검증
        - [x] (날짜, 시간, 테마, 예약 완료 상태)인 예약이 없으면, 예약 대기를 추가할 수 없다.
        - [x] 예약 완료 상태인 본인 예약이 있으면, 예약 대기를 추가할 수 없다
        - [x] 이미 본인이 추가한 예약 대기가 있으면 안된다.
        - [x] 회원은 과거 시간으로 예약 대기를 추가할 수 없다.

- 예약 대기 삭제
    - 검증
        - [x] 회원은 본인의 예약 대기만 삭제할 수 있다.