# 📌Backend API Endpoints

- AuthRole(접근 권한) 종류
    - PUBLIC: 모든 클라이언트가 접근 가능한 엔드포인트
    - ADMIN: 관리자 권한의 JWT를 가진 사용자만 접근 가능한 엔드포인트
    - MEMBER: 일반 회원 권한의 JWT를 가진 사용자만 접근 가능한 엔드포인트

## ✅ REST API

| HTTP Method | Endpoint                      | AuthRole      | Success          | Description                                                                |
|-------------|-------------------------------|---------------|------------------|----------------------------------------------------------------------------|
| POST        | /login                        | PUBLIC        | 200 OK           | 클라이언트의 로그인 요청 처리. 로그인에 성공하면 Set-Cookie 헤더에 token={access-token};을 추가하여 응답. |
| POST        | /logout                       | ADMIN, MEMBER | 200 OK           | Set-Cookie 헤더에 token의 만료기한을 0초로 세팅하여 응답 (브라우저에서 token이 삭제되길 기대)            |
| GET         | /login/check                  | PUBLIC        | 200 OK           | 클라이언트가 로그인 된 상태면 사용자의 이름을 응답으로 반환.                                         |
| GET         | /login/check                  | PUBLIC        | 401 UNAUTHORIZED | 클라이언트가 로그인되지 않은 상태면 권한 없음 반환.                                              |
| POST        | /members                      | PUBLIC        | 201 CREATED      | 회원 추가                                                                      |
| DELETE      | /members/{id}                 | ADMIN, MEMBER | 204 NO_CONTENT   | id(PK)에 해당하는 회원 삭제                                                         |
| GET         | /members                      | ADMIN         | 200 OK           | 모든 회원 목록 조회 (id(PK), 이름만)                                                  |
| POST        | /reservations                 | ADMIN, MEMBER | 201 CREATED      | 예약 추가                                                                      |
| DELETE      | /reservations/{id}            | ADMIN, MEMBER | 204 NO_CONTENT   | id(PK)에 해당하는 예약 삭제                                                         |
| GET         | /reservations                 | ADMIN         | 200 OK           | 모든 회원의 전체 예약 목록 조회                                                         |
| GET         | /admin/reservations           | ADMIN         | 200 OK           | 조건부 예약 목록 조회 (회원 id, 테마 id, 시작 기간, 끝 기간으로 필터링)                             |
| GET         | /reservations/available-times | PUBLIC        | 200 OK           | 조건부 예약 가능한 시간 목록 조회(예약 날짜, 테마 id)                                          |
| POST        | /times                        | ADMIN         | 201 CREATED      | 예약 시간 추가                                                                   |
| DELETE      | /times/{id}                   | ADMIN         | 204 NO_CONTENT   | id(PK)에 해당하는 예약 시간 삭제                                                      |
| GET         | /times                        | PUBLIC        | 200 OK           | 모든 예약 시간 목록 조회                                                             |
| POST        | /themes                       | ADMIN         | 201 CREATED      | 테마 추가                                                                      |
| DELETE      | /themes/{id}                  | ADMIN         | 204 NO_CONTENT   | id(PK)에 해당하는 테마 삭제                                                         |
| GET         | /themes                       | PUBLIC        | 200 OK           | 모든 테마 목록 조회                                                                |
| GET         | /themes/popular-list          | PUBLIC        | 200 OK           | 인기 테마 목록 조회                                                                |

## ✅ View API

| HTTP Method | Endpoint           | AuthRole | Success | Description                                                               |
|-------------|--------------------|----------|---------|---------------------------------------------------------------------------|
| GET         | /                  | PUBLIC   | 200 OK  | 방탈출 사이트 메인 페이지를 렌더링하여 응답. 현재는 인기 테마 목록을 보여줌(일주일 이내 가장 예약이 많았던 인기 테마 10가지) |
| GET         | /reservation       | PUBLIC   | 200 OK  | 일반 회원이 방탈출 예약을 할 수 있도록 도와주는 페이지를 렌더링하여 응답                                 |
| GET         | /signup            | PUBLIC   | 200 OK  | 회원가입 페이지를 렌더링하여 응답                                                        |
| GET         | /login             | PUBLIC   | 200 OK  | 로그인 페이지(login.html)을 렌더링하여 응답.                                            |
| GET         | /admin             | ADMIN    | 200 OK  | 관리자 홈 화면을 렌더링하여 응답                                                        |
| GET         | /admin/reservation | ADMIN    | 200 OK  | 관리자의 예약 내역 관리 페이지를 렌더링하여 응답                                               |
| GET         | /admin/time        | ADMIN    | 200 OK  | 관리자의 예약 시간 관리 페이지를 렌더링하여 응답                                               |
| GET         | /admin/theme       | ADMIN    | 200 OK  | 관리자의 테마 관리 페이지를 렌더링하여 응답                                                  |

# 📌 예외 처리

- 예약 추가
    - 한 테마의 날짜와 시간이 중복되는 예약을 추가할 수 없다.
    - 과거 시간으로 예약을 추가할 수 없다.
- 예약 시간 추가
    - 예약 시간은 10:00~22:00이어야 한다.
    - 예약 시간은 중복될 수 없다.
- 예약 시간 삭제
    - 해당 예약 시간으로 등록된 예약이 있으면 삭제할 수 없다.

- 테마 추가
    - 테마 이름은 중복될 수 없다.
- 테마 삭제
    - 해당 테마로 등록된 예약이 있으면 삭제할 수 없다.
