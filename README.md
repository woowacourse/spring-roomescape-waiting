# spring-roomescape-admin

방탈출 예약을 관리하는 Spring 웹 애플리케이션입니다.

## 📋 목차

- [프로젝트 구조](#-프로젝트-구조)
- [API 명세](#api-명세)
- [에러 응답 명세](#에러-응답-명세)
- [2단계 - 사용자 예약](#2단계---사용자-예약)
- [테스트 케이스](#-테스트-케이스)

---

## 🗂 프로젝트 구조

### Domain

#### Reservation

예약 정보를 관리한다.

| 필드     | 타입          | 설명 |
|--------|-------------|-----|
| `id`   | `Long`      | 예약 ID |
| `name` | `String`    | 예약자 이름 |
| `date` | `LocalDate` | 예약 날짜 |
| `time` | `LocalTime` | 예약 시간 |

#### ReservationTime

예약 시간 정보를 관리한다.

| 필드       | 타입        | 설명 |
|----------|-----------|------|
| `id`     | `Long`    | 시간 ID |
| `startAt` | `LocalTime` | 시작 시간 |

#### Theme

테마 정보를 관리한다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | `Long` | 테마 ID |
| `name` | `String` | 테마 이름 |
| `description` | `String` | 테마 설명 |

#### Holiday

영업일이 아닌 날(휴일) 정보를 관리한다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | `Long` | 휴일 ID |
| `date` | `LocalDate` | 휴일 날짜 |

---

## API 명세

| 기능 | 메서드 / URL | 요청 본문 | 응답 |
|------|-------------|-----------|------|
| 예약 전체 조회 | `GET /reservations` | — | `[{id, name, date, time}, ...]` |
| 예약 추가 | `POST /reservations` | `{name, date, timeId}` | `{id, name, date, time}` |
| 예약 삭제 | `DELETE /reservations/{id}` | — | `204 No Content` |
| 시간 전체 조회 | `GET /times` | — | `[{id, startAt}, ...]` |
| 시간 추가 | `POST /times` | `{startAt}` | `{id, startAt}` |
| 시간 삭제 | `DELETE /times/{id}` | — | `204 No Content` |
| 테마 전체 조회 | `GET /themes` | — | `[{id, name, description}, ...]` |
| 테마 추가 | `POST /themes` | `{name, description}` | `{id, name, description}` |
| 테마 삭제 | `DELETE /themes/{id}` | — | `204 No Content` |
| 사용 가능 날짜 | `GET /available-dates?month=YYYY-MM` | — | `["yyyy-MM-dd", ...]` |
| 테마별 가능 시간 | `GET /times?themeId=&date=yyyy-MM-dd` | — | `[{id, startAt, endAt}, ...]` |
| 인기 테마 조회 | `GET /themes/best?date=yyyy-MM-dd` | — | `[{id, name}, ...]` |
| 내 예약 조회 | `GET /reservations/user?name=` | — | `[{id, name, date, time, theme}, ...]` |
| 내 예약 취소 | `DELETE /reservations/user/{id}` | — | `204 No Content` |
| 내 예약 변경 | `PUT /reservations/user/{id}` | `{date, timeId}` | `{id, name, date, time, theme}` |

### 비즈니스 규칙

|  | 정책 | 위반 시 응답 |
|--|--------------------------------------|------------|
| 1 | 지나간 날짜 및 시간에 대한 예약 생성 불가             | `400 Bad Request` |
| 2 | 같은 날짜 + 시간 + 테마에 이미 예약이 있으면 중복 예약 거부 | `409 Conflict` |
| 3 | 예약이 존재하는 시간은 삭제 불가                   | `409 Conflict` |
| 3-1 | 존재하지 않는 예약 취소 불가                      | `404 Not Found` |
| 4 | 이미 지난 예약은 취소·변경 불가                   | `400 Bad Request` |
| 5 | 변경하려는 날짜·시간 슬롯이 이미 예약된 경우 변경 거부      | `409 Conflict` |

### 입력값 유효성 검증

| 도메인 | 필드 | 조건 | 위반 시 응답 |
|--------|------|------|------------|
| 예약 | `name` | 필수, 공백 불가 (`@NotBlank`) | `400 Bad Request` |
| 예약 | `date` | 필수, `yyyy-MM-dd` 형식 (`@NotNull`) | `400 Bad Request` |
| 예약 | `themeId`, `timeId` | 필수 (`@NotNull`) | `400 Bad Request` |
| 시간 | `startAt`, `endAt` | 필수, `HH:mm` 형식 (`@NotNull`) | `400 Bad Request` |
| 테마 | `name`, `description` | 필수, 공백 불가 (`@NotBlank`) | `400 Bad Request` |
| 휴일 | `date` | 필수, `yyyy-MM-dd` 형식 (`@NotNull`) | `400 Bad Request` |
| 예약 변경 | `date` | 필수, `yyyy-MM-dd` 형식 (`@NotNull`) | `400 Bad Request` |
| 예약 변경 | `timeId` | 필수 (`@NotNull`) | `400 Bad Request` |

---

## 에러 응답 명세

에러 응답은 아래 형식의 JSON 본문을 반환한다.

```json
{
  "code": "에러_코드",
  "message": "사람이 읽을 수 있는 설명"
}
```

| HTTP 상태 | `code` | `message` | 발생 상황 |
|-----------|--------|-----------|----------|
| `400` | `PAST_RESERVATION` | 과거 날짜·시간은 예약이 불가합니다. | 과거 날짜·시간으로 예약 생성, 또는 이미 지난 예약을 취소·변경 |
| `400` | `INVALID_REQUEST` | 입력값이 유효하지 않습니다. | `@NotBlank`, `@NotNull` 등 유효성 검증 실패, 또는 필수 필드 누락 |
| `400` | `INVALID_FORMAT` | 요청 형식이 올바르지 않습니다. | 날짜·시간 형식 오류 (`2026/05/20` 등) |
| `404` | `RESERVATION_NOT_FOUND` | 예약을 찾을 수 없습니다. | 존재하지 않는 예약 ID로 취소·변경 요청 |
| `404` | `TIME_NOT_FOUND` | 예약 시간을 찾을 수 없습니다. | 존재하지 않는 시간 ID 참조 |
| `404` | `THEME_NOT_FOUND` | 테마를 찾을 수 없습니다. | 존재하지 않는 테마 ID 참조 |
| `404` | `HOLIDAY_NOT_FOUND` | 휴일 정보를 찾을 수 없습니다. | 존재하지 않는 휴일 ID 삭제 요청 |
| `409` | `DUPLICATE_RESERVATION` | 중복 예약은 불가합니다. | 같은 날짜·시간·테마에 이미 예약 존재, 또는 변경하려는 시간이 이미 차 있음 |
| `409` | `TIME_IN_USE` | 해당 시간에 예약이 존재하여 삭제할 수 없습니다. | 예약이 있는 시간 삭제 시도 |
| `500` | `INTERNAL_SERVER_ERROR` | 서버 오류가 발생했습니다. | 처리되지 않은 예외 |

**에러 응답 예시**

```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "code": "RESERVATION_NOT_FOUND",
  "message": "예약을 찾을 수 없습니다."
}
```

---

## 1단계 - 방탈출 예약 관리

### GET /reservations — 예약 전체 조회

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

---

### POST /reservations — 예약 추가

**요청 예시**

```http
POST /reservations HTTP/1.1
Content-Type: application/json

{
    "name": "브라운",
    "date": "2023-08-05",
    "timeId": 1
}
```

**응답 예시**

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "time": {
        "id": 1,
        "startAt": "10:00"
    }
}
```

---

### DELETE /reservations/{id} — 예약 삭제

**응답 예시**

```http
HTTP/1.1 204 No Content
```

---

### GET /times — 시간 전체 조회

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "10:00"
    }
]
```

---

### POST /times — 시간 추가

**요청 예시**

```http
POST /times HTTP/1.1
Content-Type: application/json

{
    "startAt": "10:00"
}
```

**응답 예시**

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

---

### DELETE /times/{id} — 시간 삭제

**응답 예시**

```http
HTTP/1.1 204 No Content
```

---

## 2단계 - 사용자 예약

### GET /themes — 테마 전체 조회

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "테마명",
        "description": "테마 설명"
    }
]
```

---

### POST /themes — 테마 추가

**요청 예시**

```http
POST /themes HTTP/1.1
Content-Type: application/json

{
    "name": "테마명",
    "description": "테마 설명"
}
```

**응답 예시**

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
    "id": 1,
    "name": "테마명",
    "description": "테마 설명"
}
```

---

### DELETE /themes/{id} — 테마 삭제

**응답 예시**

```http
HTTP/1.1 204 No Content
```

---

### GET /available-dates — 사용 가능 날짜

**요청 예시** (쿼리 생략 시 전체 기간 등 구현에 따름)

```http
GET /available-dates?month=2026-05 HTTP/1.1
```

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    "2026-05-04",
    "2026-05-05",
    "2026-05-06"
]
```

---

### GET /times?themeId=&date= — 테마별 가능한 시간

**요청 예시**

```http
GET /times?themeId=1&date=2025-05-10 HTTP/1.1
```

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "10:00",
        "endAt": "12:00"
    },
    {
        "id": 2,
        "startAt": "12:00",
        "endAt": "14:00"
    }
]
```

---

### POST /reservations — 사용자 예약 추가

**요청 예시**

```http
POST /reservations HTTP/1.1
Content-Type: application/json

{
    "name": "브라운",
    "date": "2025-05-10",
    "themeId": 1,
    "timeId": 1
}
```

**응답 예시**

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2025-05-10",
    "time": {
        "id": 1,
        "startAt": "10:00",
        "endAt": "12:00"
    },
    "theme": {
        "id": 1,
        "name": "테마명"
    }
}
```

---

### GET /reservations/user?name= — 내 예약 목록 조회

**요청 예시**

```http
GET /reservations/user?name=브라운 HTTP/1.1
```

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2025-05-10",
        "time": {
            "id": 1,
            "startAt": "10:00",
            "endAt": "12:00"
        },
        "theme": {
            "id": 1,
            "name": "테마명"
        }
    }
]
```

---

### DELETE /reservations/user/{id} — 내 예약 취소

이미 지난 예약은 취소할 수 없습니다.

**응답 예시**

```http
HTTP/1.1 204 No Content
```

**에러 예시 (이미 지난 예약)**

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
    "code": "PAST_RESERVATION",
    "message": "과거 날짜·시간은 예약이 불가합니다."
}
```

---

### PUT /reservations/user/{id} — 내 예약 변경

날짜와 시간을 변경합니다. 이미 지난 예약이거나 변경하려는 시간이 이미 차 있으면 거부합니다.

**요청 예시**

```http
PUT /reservations/user/1 HTTP/1.1
Content-Type: application/json

{
    "date": "2026-06-01",
    "timeId": 2
}
```

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2026-06-01",
    "time": {
        "id": 2,
        "startAt": "14:00",
        "endAt": "16:00"
    },
    "theme": {
        "id": 1,
        "name": "테마명"
    }
}
```

**에러 예시 (시간 중복)**

```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
    "code": "DUPLICATE_RESERVATION",
    "message": "중복 예약은 불가합니다."
}
```

---

### GET /themes/best — 인기 테마 조회

**요청 예시**

```http
GET /themes/best?date=2025-05-10 HTTP/1.1
```

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "테마명"
    }
]
```

---

## ✅ 테스트 케이스

### RoomescapeApplicationTest

- [x] `contextLoads`

### MissionStepTest

- [x] `예약_조회`
- [x] `예약_추가_및_삭제`
- [x] `같은_날짜와_시간이라도_테마가_다르면_각각_예약_가능하다`
- [x] `없는_예약_삭제시_204_응답`
- [x] `시간_관리_API`
- [x] `예약과_시간_연결`
- [x] `예약_시간이_없으면_400_에러_응답`

### DatabaseMissionStepTest

- [x] `데이터베이스_연동`
- [x] `DB_조회_API_전환`
- [x] `DB_추가_삭제_API_전환`

### TimeTest

- [x] `시간_관리_API`
- [x] `없는_시간_삭제시_404_에러_응답`
- [x] `예약과_시간_연결`

### ReservationControllerTest

- [x] `예약_목록_조회_테스트`
- [x] `예약_생성_테스트`
- [x] `빈_이름_예약_생성_400_반환_테스트`
- [x] `공백_이름_예약_생성_400_반환_테스트`
- [x] `잘못된_날짜_형식_예약_생성_400_반환_테스트`
- [x] `날짜_누락_예약_생성_400_반환_테스트`
- [x] `예약_취소_테스트`
- [x] `존재하지_않는_예약_취소_404_반환_테스트`

### UserReservationControllerTest

- [x] `이름으로_예약_목록_조회_테스트`
- [x] `예약_취소_테스트`
- [x] `존재하지_않는_예약_취소_404_반환_테스트`
- [x] `지난_예약_취소_400_반환_테스트`
- [x] `예약_변경_테스트`
- [x] `존재하지_않는_예약_변경_404_반환_테스트`
- [x] `과거_날짜로_예약_변경_400_반환_테스트`
- [x] `이미_찬_시간으로_예약_변경_409_반환_테스트`
- [x] `date_누락_예약_변경_400_반환_테스트`

### ReservationServiceImplTest

- [x] `create_정상_예약을_저장하고_반환`
- [x] `create_timeId가_null이면_예외`
- [x] `create_존재하지_않는_timeId이면_예외`
- [x] `create_themeId가_null이면_예외`
- [x] `create_존재하지_않는_themeId이면_예외`
- [x] `create_휴일이면_예외`
- [x] `create_중복_예약이면_예외`
- [x] `create_과거_날짜_예약이면_예외`
- [x] `create_오늘_지난_시간_예약이면_예외`
- [x] `getAll`
- [x] `cancel_정상_취소`
- [x] `cancel_존재하지_않는_예약이면_예외`
- [x] `getByName_예약_목록_반환`
- [x] `cancelForUser_존재하지_않는_예약이면_예외`
- [x] `cancelForUser_지난_날짜_예약이면_예외`
- [x] `cancelForUser_정상_취소`
- [x] `update_존재하지_않는_예약이면_예외`
- [x] `update_과거_날짜로_변경하면_예외`
- [x] `update_중복_시간으로_변경하면_예외`
- [x] `update_정상_변경`

### JdbcReservationRepositoryTest

- [x] `findTimeIdsByThemeIdAndDate`
- [x] `findTimeIdsByThemeIdAndDate_다른테마의_예약시간은_조회하지_않는다`

### TimeControllerTest

- [x] `가능한_시간_목록_조회`
- [x] `잘못된_날짜_형식_가능한_시간_조회_400`
- [x] `startAt_누락_시간_생성_400`
- [x] `endAt_누락_시간_생성_400`
- [x] `잘못된_시간_형식_시간_생성_400`

### TimeServiceImplTest

- [x] `create_정상_저장하고_반환`
- [x] `findAll_전체_목록_반환`
- [x] `findById_정상_조회`
- [x] `findById_없는_id이면_예외`
- [x] `deleteById_정상_삭제`
- [x] `deleteById_예약이_있으면_예외`
- [x] `deleteById_없는_id이면_예외`

### ThemeControllerTest

- [x] `테마_목록_조회`
- [x] `테마_생성`
- [x] `테마_삭제`
- [x] `존재하지_않는_테마_삭제_404`
- [x] `빈_이름_테마_생성_400`
- [x] `공백_이름_테마_생성_400`
- [x] `빈_설명_테마_생성_400`

### ThemeIntegrationTest

- [x] `getAll`
- [x] `create`
- [x] `delete`
- [x] `없는_테마_삭제시_404_에러_응답`

### ThemeServiceImplTest

- [x] `getAll`
- [x] `create`
- [x] `deleteById`
- [x] `deleteById_없으면_예외`
- [x] `getAvailableTimes_테마가없으면_예외`
- [x] `getAvailableTimes_휴일이면_빈리스트`
- [x] `getAvailableTimes_예약된시간은_제외한다`
- [x] `getAvailableTimes_예약이없으면_전체시간을_반환한다`

### JdbcThemeRepositoryTest

- [x] `findAll`
- [x] `save`
- [x] `deleteById`
- [x] `existsById`
- [x] `findBestThemesByDate`

### AdminHolidayControllerTest

- [x] `휴일_목록_조회`
- [x] `휴일_생성`
- [x] `휴일_삭제`
- [x] `날짜_누락_휴일_생성_400`
- [x] `잘못된_날짜_형식_휴일_생성_400`

### HolidayServiceImplTest

- [x] `delete_throwsException_whenHolidayNotFound`

### JdbcHolidayRepositoryTest

- [x] `JdbcHolidayIsNotNull`
- [x] `save`
- [x] `findAll`
- [x] `delete`
- [x] `existsByDate`

### RoomescapePageControllerTest

- [x] `dashboardPageRenders`
- [x] `reservationsPageRendersReservationData`
- [x] `availabilityPageRendersThemeOptionsAndResults`
- [x] `createReservation_redirectsWithSuccessMessage`
- [x] `createReservation_redirectsWithSafeFailureMessage`
- [x] `deleteTheme_redirectsWithSafeFailureMessage`
- [x] `deleteTime_redirectsWithSafeFailureMessage`
- [x] `cancelReservation_redirectsWithSafeFailureMessage`
- [x] `deleteHoliday_redirectsWithSafeFailureMessage`
