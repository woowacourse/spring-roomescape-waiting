# 방탈출 API 명세

| Method | Endpoint                                         | Description               |
|--------|--------------------------------------------------|---------------------------|
| GET    | `/admin`                                         | 어드민 페이지 요청                |
| GET    | `/admin/reservation`                             | 어드민 예약 관리 페이지 요청          |
| GET    | `/admin/time`                                    | 어드민 시간 관리 페이지 요청          |
| GET    | `/admin/theme`                                   | 어드민 테마 관리 페이지 요청          |
| GET    | `/reservation`                                   | 예약 관리 페이지 요청              |
| GET    | ` `                                              | 메인 페이지 요청                 |
| GET    | `/login`                                         | 로그인 페이지 요청                |
| GET    | `/signup`                                        | 회원가입 페이지 요청               |
| GET    | `/members`                                       | 전체 회원 조회 요청               |
| POST   | `/members`                                       | 회원 가입 요청                  |
| GET    | `/reservations`                                  | 전체 예약 정보 요청               |
| POST   | `/reservations`                                  | 예약 추가 요청                  |
| DELETE | `/reservations/{id}`                             | 예약 삭제 요청                  |
| GET    | `/times`                                         | 전체 시간 요청                  |
| POST   | `/times`                                         | 시간 추가 요청                  |
| DELETE | `/times/{id}`                                    | 시간 삭제 요청                  |
| DELETE | `/times/available?date={date}&themeId={themeId}` | 특정 날짜에 특정 테마에 대한 시간 정보 요청 |
| GET    | `/themes`                                        | 전체 테마 요청                  |
| POST   | `/themes`                                        | 테마 추가 요청                  |
| DELETE | `/themes/{id}`                                   | 테마 삭제 요청                  |
| DELETE | `/themes/popular`                                | 인기 있는 테마 요청               |

# 도메인 명세

## 예약 가능 시각

- [x] 예약 가능 시간은 null일 수 없다.
- [x] 예약 가능 시간은 이미 등록되어 있는 시간과 겹칠 수 없다.
- [x] 예약 가능 시간은 정각 단위여야 한다.

## 예약

- [x] 예약 시각이 중복되는 예약이 존재하는 경우 예약을 할 수 없다.
- [x] 예약시간은 지정된 예약시간 중 하나여야 한다.
- [x] 예약 테마는 존재하는 테마여야 한다.

## 멤버

- [x] 멤버 이름은 빈 문자열일 수 없다.
- [x] 멤버 이름은 Null일 수 없다.
- [x] 멤버 이름은 공백 문자로만 이루어질 수 없다
- [x] 멤버 간 이메일이 중복될 수 없다
- [x] 멤버 간 이름이 중복될 수 없다

## 예약 날짜

- [x] 예약 신청 날짜는 오늘보다 이전일 수 없다.
- [x] 예약 날짜는 null일 수 없다.


