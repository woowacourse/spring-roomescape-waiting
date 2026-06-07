# 구현할 기능 목록

### 방탈출 에약 대기 - 사이클 2: 예약 대기 승인

- [x] 예약 취소가 발생하면 예약 대기가 존재하는지 확인 후, 예약 대기를 승인 처리한다.
  - [x] 예약 대기가 존재하지 않는다면, 예약 삭제만 수행한다.
  - [x] 예약 대기가 존재한다면, 예약 대기를 승인해 자동으로 예약 대기 순번 1번을 예약으로 전환한다.
    - [x] 예약 취소 요청이 들어온 기존 예약을 취소 처리한다.
    - [x] 예약 대기 순번 1번의 에약 대기를 승인 처리한다.
- [x] 예약 추가 시도 시, 예약 대기가 존재하는지 확인하는 로직을 추가한다.
- [ ] 예약 대기 추가 시도시, 기존 예약 대기 내역이 존재하는지 확인하는 로직을 추가한다. (존재한다면 예약 대기 추가 가능)
- [ ] 예약 변경 시도 시, 예약 대기가 존재하는지 확인하는 로직을 추가한다.
- [ ] 예약 변경 시, 해당 예약 건의 예약 대기를 승인하는 로직을 추가한다.
- [ ] 예약 대기 승인 처리에 실패한 경우 관리자 페이지에 로그를 남긴다.

---

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
