# API 명세

## 🎮 사용자 API

### 예약 (`/reservations`)

| HTTP Method & URL                                                       | 설명                       | 요청 본문                      | 응답 본문                               |
|:------------------------------------------------------------------------|:-------------------------|:---------------------------|:------------------------------------|
| `GET` `/reservations/available-dates`                                   | 예약 가능한 날짜 목록 조회          | -                          | `AvailableDateResult`               |
| `GET` `/reservations/available-times?date=yyyy-MM-dd&themeId={themeId}` | 특정 날짜/테마의 예약 가능 시간 목록 조회 | -                          | `List<ReservationTimeStatusResult>` |
| `GET` `/reservations?name={name}`                                       | 내 예약 목록 조회               | -                          | `List<ReservationResult>`           |
| `POST` `/reservations`                                                  | 신규 예약 생성                 | `ReservationCreateCommand` | `ReservationResult`                 |
| `PATCH` `/reservations/{reservation-id}`                                | 내 예약 수정                  | `ReservationModifyRequest` | `ReservationResult`                 |
| `DELETE` `/reservations/{reservation-id}?name={name}`                   | 내 예약 취소                  | -                          | -                                   |

### 예약 대기 (`/waiting-list`)

| HTTP Method & URL                 | 설명            | 요청 본문                      | 응답 본문                     |
|:----------------------------------|:--------------|:---------------------------|:--------------------------|
| `GET` `/waiting-list?name={name}` | 내 예약 대기 목록 조회 | -                          | `List<WaitingListResult>` |
| `POST` `/waiting-list`            | 신규 예약 대기 생성   | `WaitingListCreateCommand` | `WaitingListResult`       |
| `DELETE` `/waiting-list/{id}`     | 내 예약 대기 취소    | `WaitingListDeleteRequest` | -                         |

<br>

## 🔑 관리자 API

### 예약 관리 (`/admin/reservations`)

| HTTP Method & URL                               | 설명          | 요청 본문 | 응답 본문                     |
|:------------------------------------------------|:------------|:------|:--------------------------|
| `GET` `/admin/reservations`                     | 전체 예약 목록 조회 | -     | `List<ReservationResult>` |
| `DELETE` `/admin/reservations/{reservation-id}` | 특정 예약 삭제    | -     | -                         |
### 예약 시간 관리 (`/times`)

| HTTP Method & URL           | 설명             | 요청 본문                          | 응답 본문                         |
|:----------------------------|:---------------|:-------------------------------|:------------------------------|
| `GET` `/times`              | 전체 예약 시간 목록 조회 | -                              | `List<ReservationTimeResult>` |
| `POST` `/times`             | 신규 예약 시간 생성    | `ReservationTimeCreateCommand` | `ReservationTimeResult`       |
| `DELETE` `/times/{time-id}` | 특정 예약 시간 삭제    | -                              | -                             |

### 테마 관리 (`/themes`)

| HTTP Method & URL             | 설명          | 요청 본문                | 응답 본문               |
|:------------------------------|:------------|:---------------------|:--------------------|
| `GET` `/themes`               | 전체 테마 목록 조회 | -                    | `List<ThemeResult>` |
| `GET` `/themes/popular`       | 인기 테마 목록 조회 | -                    | `List<ThemeResult>` |
| `POST` `/themes`              | 신규 테마 생성    | `ThemeCreateCommand` | `ThemeResult`       |
| `DELETE` `/themes/{theme-id}` | 특정 테마 삭제    | -                    | -                   |
