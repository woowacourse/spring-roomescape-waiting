# 🚪방탈출 사용자 예약 애플리케이션

`RoomescapeApplication`에서 실행 가능합니다.

### 관리자 페이지

* http://localhost:8080/admin/time: 사용자가 예약할 수 있는 시간 관리 페이지
* http://localhost:8080/admin/theme: 사용자가 이용할 수 있는 방탈출 테마 관리 페이지
* http://localhost:8080/admin/reservation: 사용자 예약 관리 페이지
* http://localhost:8080/admin/waiting: 사용자 예약 대기 관리 페이지

### 사용자 페이지

* http://localhost:8080/: 사용자 예약 기준으로 탑10 방탈출 테마 확인 페이지
* http://localhost:8080/reservation: 사용자가 예약을 할 수 있는 페이지
* http://localhost:8080/reservation-mine: 사용자가 자신의 예약 목록을 확인할 수 있는 페이지
* http://localhost:8080/signup: 사용자가 회원가입을 할 수 있는 페이지
* http://localhost:8080/login: 사용자가 로그인을 할 수 있는 페이지

## 요구사항 분석

### 1. JPA를 활용하여 데이터베이스에 접근하도록 수정

- [x] `Member`, `Theme`, `ReservationTime`을 엔티티로 변환
- [x] `Reservation`를 엔티티로 변환
- [x] `Member`, `Theme`, `ReservationTime`와 연관관계 매핑 (단방향)
    - [x] `Reservation` : `Member` = N : 1
    - [x] `Reservation` : `Theme` = N : 1
    - [x] `Reservation` : `ReservationTime` = N : 1
- [x] 테마와 시간이 같은 예약은 하나만 존재해야한다.(alreadyBooked)
- [x] 예약이 존재하는 시간과 테마는 삭제할 수 없다.

### 2. 내 예약 목록 조회

- [x] `/reservation-mine` 요청 시, 사용자 예약 정보를 응답 받을 수 있다.
- [x] 성공된 예약은 상태를 포함할 수 있다.

### 3. 사용자 예약 대기 기능

- [x] 사용자는 예약이 있는 시간대에 예약 대기 요청을 보낼 수 있다.
    - [x] 사용자 한 명이 동일한 날짜, 테마, 시간을 예약 불가능
- [x] 사용자는 예약 대기를 취소할 수 있다.
- [x] 내 예약 목록 조회 시 예약 대기 목록도 포함된다.
    - [x] 몇 번째 대기 상태인지도 출력

### 4. 관리자 예약 대기 관리 기능

- [x] 어드민은 예약 대기 목록을 조회할 수 있다.
- [x] 어드민은 예약 대기를 승인/거절할 수 있다.
    - [x] 예약 취소가 발생하면 예약 대기자가 있는 경우 예약을 승인(수동)
    - [x] 예약이 취소되지 않았다면 예약을 승인할 수 없음

---

## API 목록

### 홈(Home)

| URL         | 메서드 | 기능           |
|-------------|-----|--------------|
| `/`         | GET | 인기 테마 페이지 보기 |
| `/top-rank` | GET | 인기 테마 조회     |

### 관리자(Admin)

| URL                    | 메서드    | 기능              |
|------------------------|--------|-----------------|
| `/admin`               | GET    | 어드민 페이지 보기      |
| `/admin/reservation`   | GET    | 예약 관리 페이지 보기    |
| `/admin/time`          | GET    | 예약 시간 관리 페이지 보기 |
| `/admin/theme`         | GET    | 테마 관리 페이지 보기    |
| `/admin/waiting`       | GET    | 예약 대기 관리 페이지 보기 |
| `/admin/reservations`  | POST   | 예약 추가           |
| `/reservations/{id}`   | DELETE | 예약 삭제           |
| `/reservations/filter` | GET    | 예약 필터링 조회       |
| `/members`             | GET    | 사용자 모두 조회       |
| `/times`               | GET    | 예약 가능 시간 모두 조회  |
| `/times`               | POST   | 예약 시간 추가        |
| `/times/{id}`          | DELETE | 예약 시간 삭제        |
| `/themes`              | POST   | 테마 추가           |
| `/themes/{id}`         | DELETE | 테마 삭제           |
| `/waitings`            | GET    | 예약 대기 목록 조회     |
| `/waitings/{id}`       | PATCH  | 예약 대기 승인/거절     |

### 사용자(User)

| URL                       | 메서드    | 기능                 |
|---------------------------|--------|--------------------|
| `/reservation`            | GET    | 사용자 예약 페이지 보기      |
| `/reservations`           | POST   | 사용자 페이지에서 예약 추가    |
| `/waitings`               | POST   | 사용자 페이지에서 예약 대기 추가 |
| `/reservations/mine`      | GET    | 사용자 예약 목록 조회       |
| `/reservations/mine/{id}` | DELETE | 사용자 예약 대기 삭제       |
| `/times/{date}/{themeId}` | GET    | 예약 가능한 상태의 시간 조회   |
| `/members`                | POST   | 사용자 회원가입 추가        |
| `/signup`                 | GET    | 사용자 회원가입 페이지 보기    |
| `/login`                  | GET    | 사용자 로그인 페이지 보기     |
| `/login`                  | POST   | 사용자 로그인 후 토큰 생성    |
| `/login/check`            | GET    | 사용자 인증 정보 확인       |
| `/logout`                 | POST   | 사용자 로그아웃 후 토큰 만료   |

### Common

| URL             | 메서드 | 기능       |
|-----------------|-----|----------|
| `/reservations` | GET | 예약 목록 조회 |
| `/themes`       | GET | 테마 조회    |

### 예외 응답 형태

```
{
  "timestamp": "2025-05-01T13:55:28.074+00:00",
  "code": "NOT_FOUND",
  "message": "해당 정보를 찾을 수 없습니다: theme"
}
```
