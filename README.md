# 방탈출 예약 미션

## 접속 방법

1. 애플리케이션 실행
2. http://localhost:8080/ 로 접속

## 🗒️ 용어 정의 및 모델링

### 예약

| 용어          | 설명                                       |
|-------------|------------------------------------------|
| reservation | 방탈출 예약 정보. 예약자 이름과 방문 날짜, 예약 시각, 테마를 포함. |
| name        | 예약자 이름. 30자 이내.                          |
| date        | 예약 날짜.                                   |
| time        | 예약 시각. 시간은 슬롯으로 관리된다.                    |
| theme       | 방탈출 테마.                                  |
| slot        | 날짜, 예약 시간, 테마를 함께 묶은 예약 단위.              |

### 예약 시간

| 용어               | 설명                       |
|------------------|--------------------------|
| reservation time | 예약 시간                    |
| start at         | 시간을 슬롯으로 관리할 때 슬롯의 시작 시간 |

### 테마

| 용어                  | 설명     |
|---------------------|--------|
| theme               | 방탈출 테마 |
| theme name          | 테마 이름  |
| theme description   | 테마 설명  |
| theme thumbnail url | 테마 썸네일 |

### 예약 대기

| 용어            | 설명                                 |
|---------------|------------------------------------|
| waiting       | 이미 예약된 날짜+시간+테마 슬롯에 신청하는 예약 대기 정보. |
| order index   | 같은 슬롯 안에서의 대기 순번. 신청 순서대로 부여된다.    |
| status        | 내 예약 조회에서 예약과 대기를 구분하는 상태값.        |
| waiting order | 내 예약 조회 응답에 포함되는 사용자의 대기 순번.       |

## 📝 API 명세

### 어드민 API

| 기능    | 메서드 / URL                         | 요청 본문                               | 응답 코드            | 응답 본문                                                                                        |
|-------|-----------------------------------|-------------------------------------|------------------|----------------------------------------------------------------------------------------------|
| 예약 조회 | `GET /admin/reservations`         |                                     | `200 OK`         | `[{id, name, date, time: {id, startAt}, theme: {id, name, description, thumbnailUrl}}, ...]` |
| 예약 추가 | `POST /admin/reservations`        | `{name, date, timeId, themeId}`     | `201 Created`    | `{id, name, date, time: {id, startAt}, theme: {id, name, description, thumbnailUrl}}`        |
| 예약 삭제 | `DELETE /admin/reservations/{id}` |                                     | `204 No Content` |                                                                                              |
| 시간 조회 | `GET /admin/times`                |                                     | `200 OK`         | `[{id, startAt}, ...]`                                                                       |
| 시간 추가 | `POST /admin/times`               | `{startAt}`                         | `201 Created`    | `{id, startAt}`                                                                              |
| 시간 삭제 | `DELETE /admin/times/{id}`        |                                     | `204 No Content` |                                                                                              |
| 테마 조회 | `GET /admin/themes`               |                                     | `200 OK`         | `[{id, name, description, thumbnailUrl}, ...]`                                               |
| 테마 추가 | `POST /admin/themes`              | `{name, description, thumbnailUrl}` | `201 Created`    | `{id, name, description, thumbnailUrl}`                                                      |
| 테마 삭제 | `DELETE /admin/themes/{id}`       |                                     | `204 No Content` |                                                                                              |

### 사용자 API

| 기능           | 메서드 / URL                                                    | 요청 본문                           | 응답 코드            | 응답 본문                                                                                                         |
|--------------|--------------------------------------------------------------|---------------------------------|------------------|---------------------------------------------------------------------------------------------------------------|
| 예약 추가        | `POST /user/reservations`                                    | `{name, date, timeId, themeId}` | `201 Created`    | `{id, name, date, time: {id, startAt}, theme: {id, name, description, thumbnailUrl}}`                         |
| 이름으로 예약 조회   | `GET /user/reservations?name={name}`                         |                                 | `200 OK`         | `[{id, date, time: {id, startAt}, theme: {id, name, description, thumbnailUrl}, status, waitingOrder?}, ...]` |
| 이름으로 예약 삭제   | `DELETE /user/reservations/{id}?name={name}`                 |                                 | `204 No Content` |                                                                                                               |
| 예약 변경        | `PATCH /user/reservations/{id}`                              | `{name, date, timeId}`          | `200 OK`         | `{id, name, date, time: {id, startAt}, theme: {id, name, description, thumbnailUrl}}`                         |
| 테마 조회        | `GET /user/themes`                                           |                                 | `200 OK`         | `[{id, name, description, thumbnailUrl}, ...]`                                                                |
| 예약 가능한 시간 조회 | `GET /user/themes/{themeId}/available-times?date=YYYY-MM-DD` |                                 | `200 OK`         | `[{id, startAt}, ...]`                                                                                        |
| 인기 있는 테마 조회  | `GET /user/themes/popular`                                   |                                 | `200 OK`         | `[{id, name, description, thumbnailUrl, reservationCount}, ...]`                                              |
| 예약 대기 신청     | `POST /user/waitings`                                        | `{name, date, timeId, themeId}` | `201 Created`    | `{id, name, date, time: {id, startAt}, theme: {id, name, description, thumbnailUrl}, orderIndex}`             |
| 예약 대기 취소     | `DELETE /user/waitings/{id}?name={name}`                     |                                 | `204 No Content` |                                                                                                               |

## 예외 처리

에러 시 적절한 응답 코드와 함께 `{ "message": "에러 메시지" }` 로 반환한다.

### 예외 상황 및 에러 메시지

| 상황                          | 응답 코드                       | 메시지                                     |
|-----------------------------|-----------------------------|-----------------------------------------|
| 요청 본문의 필수값이 없음              | `400 Bad Request`           | 각 필드 검증 메시지. 예: `예약자 이름은 비어 있을 수 없습니다.` |
| 필수 query parameter가 없음      | `400 Bad Request`           | `필수 요청 파라미터가 누락되었습니다.`                  |
| 요청 본문의 형식이 잘못됨              | `400 Bad Request`           | `요청 본문의 형식이 올바르지 않습니다.`                 |
| 잘못된 path/query parameter 형식 | `400 Bad Request`           | `요청 파라미터 형식이 올바르지 않습니다.`                |
| 존재하지 않는 URL 요청              | `404 Not Found`             | `요청하신 리소스를 찾을 수 없습니다.`                  |
| 지원하지 않는 HTTP 메서드 요청         | `400 Bad Request`           | `지원하지 않는 요청 방식입니다.`                     |
| 존재하지 않는 예약 시간으로 예약 생성       | `404 Not Found`             | `존재하지 않는 시간입니다.`                        |
| 존재하지 않는 예약 시간으로 예약 수정       | `404 Not Found`             | `존재하지 않는 시간입니다.`                        |
| 존재하지 않는 테마로 예약 생성           | `404 Not Found`             | `존재하지 않는 테마입니다.`                        |
| 존재하지 않는 예약 삭제               | `404 Not Found`             | `존재하지 않는 예약입니다.`                        |
| 존재하지 않는 예약 수정               | `404 Not Found`             | `존재하지 않는 예약입니다.`                        |
| 수정/삭제하려는 예약의 이름이 입력한 이름과 다름 | `404 Not Found`             | `존재하지 않는 예약입니다.`                        |
| 같은 날짜+시간+테마에 이미 예약이 있음      | `400 Bad Request`           | `해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요.`    |
| 예약이 존재하는 시간을 삭제             | `400 Bad Request`           | `예약이 존재하는 시간은 삭제할 수 없습니다.`              |
| 예약이 존재하는 테마를 삭제             | `400 Bad Request`           | `예약이 존재하는 테마는 삭제할 수 없습니다.`              |
| 지나간 날짜·시간에 대한 예약 생성         | `400 Bad Request`           | `지나간 날짜, 시간으로는 예약할 수 없습니다.`             |
| 이미 지난 예약 변경                 | `400 Bad Request`           | `이미 지난 예약은 변경할 수 없습니다.`                 |
| 지나간 날짜·시간에 대한 예약 수정         | `400 Bad Request`           | `지나간 날짜, 시간으로는 변경할 수 없습니다.`             |
| 이미 지난 예약 취소                 | `400 Bad Request`           | `이미 지난 예약은 취소할 수 없습니다.`                 |
| 예약이 없는 슬롯에 대기 신청            | `400 Bad Request`           | `예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요.`     |
| 이미 본인이 예약한 슬롯에 대기 신청        | `400 Bad Request`           | `이미 본인이 예약한 시간에는 대기를 신청할 수 없습니다.`       |
| 같은 사용자가 같은 슬롯에 중복 대기        | `400 Bad Request`           | `이미 해당 시간에 대기 신청한 내역이 있습니다.`            |
| 존재하지 않는 대기 취소               | `404 Not Found`             | `존재하지 않는 대기입니다.`                        |
| 본인이 아닌 대기 취소                | `404 Not Found`             | `존재하지 않는 대기입니다.`                        |
| 서버에서 예상하지 못한 오류 발생          | `500 Internal Server Error` | `서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.` |

## 응답 코드

| 응답 코드                       | 상황                                |
|-----------------------------|-----------------------------------|
| `200 OK`                    | 정상적으로 조회 또는 수정됨.                  |
| `201 Created`               | 정상적으로 생성됨.                        |
| `204 No Content`            | 반환값이 없음.                          |
| `400 Bad Request`           | 클라이언트 요청값이 올바르지 않거나 비즈니스 규칙을 위반함. |
| `404 Not Found`             | 없는 자원에 대한 접근 또는 본인 소유가 아닌 자원 접근.  |
| `500 Internal Server Error` | 서버 내부 오류.                         |
