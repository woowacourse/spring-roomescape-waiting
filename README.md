# spring-roomescape-waiting

방탈출 예약 대기를 관리하는 Spring 웹 애플리케이션입니다.

## 📋 목차

- [프로젝트 구조](#-프로젝트-구조)
- [API 명세](#api-명세)
- [에러 응답 명세](#에러-응답-명세)
- [3단계 - 예약 대기](#3단계---예약-대기)

---

## 🗂 프로젝트 구조

### Domain

#### Reservation

예약 정보를 관리한다.

| 필드          | 타입               | 설명 |
|-------------|------------------|-----|
| `id`        | `Long`           | 예약 ID |
| `name`      | `String`         | 예약자 이름 |
| `time`      | `ReservationTime` | 예약 시간 |
| `theme`     | `Theme`          | 테마 정보 |
| `status`    | `Status`         | 예약 상태 (`RESERVED`, `WAITING`) |
| `createdAt` | `LocalDateTime`  | 예약 생성 시각 |

> 기존 `time_id`, `theme_id` 복합 유니크 키를 제거하고 `status`, `created_at` 필드를 추가하여 대기 상태를 함께 관리한다.

#### ReservationTime

예약 시간 정보를 관리한다.

| 필드        | 타입             | 설명 |
|-----------|----------------|------|
| `id`      | `Long`         | 시간 ID |
| `startAt` | `LocalDateTime` | 시작 날짜·시간 |
| `endAt`   | `LocalDateTime` | 종료 날짜·시간 |

#### Theme

테마 정보를 관리한다.

| 필드            | 타입       | 설명     |
|---------------|----------|--------|
| `id`          | `Long`   | 테마 ID  |
| `name`        | `String` | 테마 이름  |
| `description` | `String` | 테마 설명  |
| `imageUrl`    | `String` | 테마 이미지 |

#### Holiday

영업일이 아닌 날(휴일) 정보를 관리한다.

| 필드     | 타입          | 설명 |
|--------|-------------|------|
| `id`   | `Long`      | 휴일 ID |
| `date` | `LocalDate` | 휴일 날짜 |

---

## API 명세

| 기능 | 메서드 / URL | 요청 본문 | 응답 |
|------|-------------|-----------|------|
| 예약 생성 (대기 포함) | `POST /reservations` | `{name, themeId, timeId}` | `{id, name, status, createdAt, time, theme}` |
| 예약·대기 취소 | `DELETE /reservations/{id}?name=` | — | `204 No Content` |
| 내 예약 목록 조회 | `GET /reservations?name=` | — | `[{id, name, time, theme, status, waitingOrder}, ...]` |

### 비즈니스 규칙
|  | 정책                                                           | 위반 시 응답 |
|--|--------------------------------------------------------------|------------|
| 1 | 지나간 날짜 및 시간에 대한 예약 생성 불가                                    | `400 Bad Request` |
| 2 | 같은 슬롯(시간, 테마)에 이미 `RESERVED` 예약이 있으면 `WAITING` 상태로 대기 등록   | — |
| 3 | 존재하지 않는 예약 취소 불가                                             | `404 Not Found` |
| 4 | 이미 지난 예약은 취소, 변경 불가                                          | `400 Bad Request` |

---

## 3단계 - 예약 대기

### POST /reservations — 예약 생성 (대기 포함)

같은 슬롯(시간, 테마)에 이미 `RESERVED` 상태 예약이 존재하면 `WAITING` 상태로 대기를 등록합니다.

**요청 예시**

```http
POST /reservations HTTP/1.1
Content-Type: application/json

{
    "name": "라이",
    "timeId": 1,
    "themeId": 1
}
```

**응답 예시**

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
    "id": 1,
    "name": "라이",
    "status": "WAITING",
    "createdAt": "2025-05-27T10:00",
    "time": {
        "id": 1,
        "startAt": "2025-05-27T10:00",
        "endAt": "2025-05-27T12:00"
    },
    "theme": {
        "id": 1,
        "name": "라이츄",
        "description": "라이츄 테마 설명",
        "imageUrl": "https://example.com/image.png"
    }
}
```

---

### DELETE /reservations/{id}?name= — 예약·대기 취소

본인의 예약 또는 대기를 취소합니다. `name` 파라미터로 본인 확인을 합니다.

**요청 예시**

```http
DELETE /reservations/1?name=라이 HTTP/1.1
```

**응답 예시**

```http
HTTP/1.1 204 No Content
```

---

### GET /reservations?name= — 내 예약 목록 조회

이름으로 본인의 예약 및 대기 목록을 조회합니다. `status` 필드로 예약 상태(`RESERVED` / `WAITING`)를 구분하고, `waitingOrder`로 대기 순번을 확인할 수 있습니다 (`0`이면 예약 확정).

**요청 예시**

```http
GET /reservations?name=어셔 HTTP/1.1
```

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "어셔",
        "time": {
            "id": 1,
            "startAt": "2025-05-27T10:00",
            "endAt": "2025-05-27T12:00"
        },
        "theme": {
            "id": 1,
            "name": "어셔오셔요",
            "description": "테마 설명",
            "imageUrl": "https://example.com/image.png"
        },
        "status": "RESERVED",
        "waitingOrder": 0
    },
    {
        "id": 5,
        "name": "어셔",
        "time": {
            "id": 2,
            "startAt": "2025-05-28T14:00",
            "endAt": "2025-05-28T16:00"
        },
        "theme": {
            "id": 2,
            "name": "어셔가셔요",
            "description": "다른 테마 설명",
            "imageUrl": "https://example.com/image2.png"
        },
        "status": "WAITING",
        "waitingOrder": 2
    }
]
```