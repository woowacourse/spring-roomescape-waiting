# 🚪방탈출 사용자 예약 애플리케이션

`RoomescapeApplication`에서 실행 가능합니다.

### 관리자 페이지

* http://localhost:8080/admin/time: 사용자가 예약할 수 있는 시간 관리 페이지
* http://localhost:8080/admin/theme: 사용자가 이용할 수 있는 방탈출 테마 관리 페이지
* http://localhost:8080/admin/reservation: 사용자 예약 관리 페이지

### 사용자 페이지

* http://localhost:8080/: 사용자 예약 기준으로 탑10 방탈출 테마 확인 페이지
* http://localhost:8080/reservation: 사용자가 예약을 할 수 있는 페이지
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
- [ ] 예약이 존재하는 시간과 테마는 삭제할 수 없다.

### 2. 내 예약 목록 조회

- [ ] `/reservation-mine` 요청 시, 사용자 예약 정보를 응답 받을 수 있다.
- [ ] 성공된 예약은 상태를 포함할 수 있다.

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
| `/admin/reservations`  | POST   | 예약 추가           |
| `/reservations/{id}`   | DELETE | 예약 삭제           |
| `/reservations/filter` | GET    | 예약 필터링 조회       |
| `/members`             | GET    | 사용자 모두 조회       |
| `/times`               | GET    | 예약 가능 시간 모두 조회  |
| `/times`               | POST   | 예약 시간 추가        |
| `/times/{id}`          | DELETE | 예약 시간 삭제        |
| `/themes`              | POST   | 테마 추가           |
| `/themes/{id}`         | DELETE | 테마 삭제           |

### 사용자(User)

| URL                       | 메서드  | 기능               |
|---------------------------|------|------------------|
| `/reservation`            | GET  | 사용자 예약 페이지 보기    |
| `/reservations`           | POST | 사용자 페이지에서 예약 추가  |
| `/reservations-mine`      | GET  | 사용자 예약 목록 조회     |
| `/times/{date}/{themeId}` | GET  | 예약 가능한 상태의 시간 조회 |
| `/members`                | POST | 사용자 회원가입 추가      |
| `/signup`                 | GET  | 사용자 회원가입 페이지 보기  |
| `/login`                  | GET  | 사용자 로그인 페이지 보기   |
| `/login`                  | POST | 사용자 로그인 후 토큰 생성  |
| `/login/check`            | GET  | 사용자 인증 정보 확인     |
| `/logout`                 | POST | 사용자 로그아웃 후 토큰 만료 |

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
