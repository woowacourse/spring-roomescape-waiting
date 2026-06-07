# API 명세

## 🎮 사용자 API

### 예약 (`/reservations`)

| HTTP Method | URL                                                               | 설명 | 요청 본문                      | 응답 본문                               | 성공 응답 | 예외 응답 |
| :--- |:------------------------------------------------------------------| :--- |:---------------------------|:------------------------------------|:--- |:--- |
| `GET` | `/reservations/available-dates`                                   | 예약 가능한 날짜 목록 조회 | -                          | `AvailableDateResult`               | `200 OK` | - |
| `GET` | `/reservations/available-times?date=yyyy-MM-dd&themeId={themeId}` | 특정 날짜/테마의 예약 가능 시간 목록 조회 | -                          | `List<ReservationTimeStatusResult>` | `200 OK` | `400 Bad Request` |
| `GET` | `/reservations?name={name}`                                       | 내 예약 목록 조회 | -                          | `List<ReservationResult>`           | `200 OK` | `400 Bad Request` |
| `POST` | `/reservations`                                                   | 신규 예약 생성 | `ReservationCreateCommand` | `ReservationResult`                 | `201 Created` | `400 Bad Request`, `422 Unprocessable Entity` |
| `PATCH` | `/reservations/{reservation-id}`                                  | 내 예약 수정 | `ReservationModifyRequest` | `ReservationResult`                 | `200 OK` | `400 Bad Request`, `403 Forbidden` |
| `DELETE` | `/reservations/{reservation-id}?name={name}`                      | 내 예약 취소 | -                          | -                                   | `204 No Content` | `403 Forbidden`, `404 Not Found` |

### 예약 대기 (`/waiting-list`)

| HTTP Method | URL | 설명 | 요청 본문                      | 응답 본문                     | 성공 응답 | 예외 응답 |
| :--- | :--- | :--- |:---------------------------|:--------------------------|:--- |:--- |
| `GET` | `/waiting-list?name={name}` | 내 예약 대기 목록 조회 | -                     | `List<WaitingListResult>` | `200 OK` | `400 Bad Request` |
| `POST` | `/waiting-list` | 신규 예약 대기 생성 | `WaitingListCreateCommand` | `WaitingListResult`       | `201 Created` | `422 Unprocessable Entity` |
| `DELETE` | `/waiting-list/{id}` | 내 예약 대기 취소 | `WaitingListDeleteRequest` | -                         | `204 No Content` | `403 Forbidden`, `404 Not Found` |

<br>

## 🔑 관리자 API

### 예약 관리 (`/admin/reservations`)

| HTTP Method | URL | 설명 | 요청 본문 | 응답 본문                     | 성공 응답 | 예외 응답 |
| :--- | :--- | :--- |:------|:--------------------------|:--- |:--- |
| `GET` | `/admin/reservations` | 전체 예약 목록 조회 | -     | `List<ReservationResult>` | `200 OK` | - |
| `DELETE` | `/admin/reservations/{reservation-id}` | 특정 예약 삭제 | -     | -                         | `204 No Content` | `404 Not Found` |

### 예약 시간 관리 (`/times`)

| HTTP Method | URL | 설명 | 요청 본문                          | 응답 본문                         | 성공 응답 | 예외 응답 |
| :--- | :--- | :--- |:-------------------------------|:------------------------------|:--- |:--- |
| `GET` | `/times` | 전체 예약 시간 목록 조회 | -                              | `List<ReservationTimeResult>` | `200 OK` | - |
| `POST` | `/times` | 신규 예약 시간 생성 | `ReservationTimeCreateCommand` | `ReservationTimeResult`       | `201 Created` | `400 Bad Request` |
| `DELETE` | `/times/{time-id}` | 특정 예약 시간 삭제 | -                              | -                             | `204 No Content` | `404 Not Found`, `409 Conflict` |

### 테마 관리 (`/themes`)

| HTTP Method | URL | 설명 | 요청 본문                | 응답 본문               | 성공 응답 | 예외 응답 |
| :--- | :--- | :--- |:---------------------|:--------------------|:--- |:--- |
| `GET` | `/themes` | 전체 테마 목록 조회 | -                    | `List<ThemeResult>` | `200 OK` | - |
| `GET` | `/themes/popular` | 인기 테마 목록 조회 | -                    | `List<ThemeResult>` | `200 OK` | - |
| `POST` | `/themes` | 신규 테마 생성 | `ThemeCreateCommand` | `ThemeResult`       | `201 Created` | `400 Bad Request` |
| `DELETE` | `/themes/{theme-id}` | 특정 테마 삭제 | -                    | -                   | `204 No Content` | `404 Not Found`, `409 Conflict` |
