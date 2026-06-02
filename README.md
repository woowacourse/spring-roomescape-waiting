# 방탈출 예약 대기 (Room Escape Waiting)

## 도메인 정책

- 예약/예약 대기는 **지난 날짜·시간**으로 신청할 수 없다.
- 같은 사용자가 **동일한 (날짜·시간·테마)** 에 중복으로 대기할 수 없다.
- 이미 **본인의 예약이 있는** (날짜·시간·테마) 에는 대기를 신청할 수 없다.
- 예약 대기는 **본인만** 취소할 수 있다.
- 대기 순번은 같은 (날짜·시간·테마) 안에서 자신보다 먼저 신청한 대기 수 + 1 로 계산한다.

### 예약 대기

#### 예약 대기 신청

```http
POST /waitings
Content-Type: application/json

{
  "name": "레서",
  "date": "2026-06-20",
  "timeId": 1,
  "themeId": 1
}
```

**`201 Created`**

```json
{
  "id": 1,
  "name": "레서",
  "date": "2026-06-20",
  "time": { "id": 1, "startAt": "10:00" },
  "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" },
  "order": 1
}
```

| 상황 | 응답 |
|---|---|
| 존재하지 않는 `timeId` / `themeId` | `404 Not Found` |
| 지난 날짜·시간 | `409 Conflict` (`DOMAIN_CONFLICT`) |
| 이미 대기 중인 (날짜·시간·테마) | `409 Conflict` (`DUPLICATE_WAITING`) |
| 이미 본인이 예약한 (날짜·시간·테마) | `409 Conflict` (`DUPLICATE_RESERVATION`) |

#### 예약 대기 취소

```http
DELETE /waitings/{id}?name=레서
```

**`204 No Content`**

| 상황 | 응답 |
|---|---|
| 존재하지 않는 대기 `id` | `404 Not Found` |
| 본인 소유가 아닌 대기 | `409 Conflict` (`DOMAIN_CONFLICT`) |

### 내 예약 목록 조회 (예약 + 대기 통합)

본인의 **확정 예약(`RESERVED`)** 과 **대기(`WAITING`)** 를 함께 반환합니다. 대기 항목은 `rank`(대기 순번)를 가집니다.

```http
GET /reservations?name=레서&page=0&size=10
```

**`200 OK`**

```json
{
  "userReservations": [
    {
      "id": 1,
      "name": "레서",
      "date": "2026-06-20",
      "time": { "id": 1, "startAt": "10:00" },
      "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" },
      "status": "RESERVED",
      "rank": 0
    },
    {
      "id": 5,
      "name": "레서",
      "date": "2026-06-21",
      "time": { "id": 1, "startAt": "10:00" },
      "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" },
      "status": "WAITING",
      "rank": 2
    }
  ]
}
```

### 그 외 엔드포인트

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/reservations` | 예약 생성 |
| `PATCH` | `/reservations/{id}?name=` | 본인 예약 날짜·시간 변경 |
| `DELETE` | `/reservations/{id}?name=` | 본인 예약 취소 |
| `GET` | `/times` | 예약 시간 목록 조회 |
| `GET` | `/themes` | 테마 목록 조회 |
| `GET` | `/themes/{id}/available-times` | 특정 테마·날짜의 예약 가능 시간 조회 |
| `GET` | `/themes/popular` | 인기 테마 조회 |
| `GET` | `/admin/reservations?page=&size=` | (관리자) 전체 예약 목록 조회 |
| `POST`/`DELETE` | `/admin/times`, `/admin/themes` | (관리자) 시간·테마 관리 |

## 에러 응답 형식

모든 에러는 아래 형식으로 반환됩니다.

```json
{
  "code": "DUPLICATE_WAITING",
  "message": "이미 대기 중인 시간입니다"
}
```

| HTTP 상태 | 대표 `code` |
|---|---|
| `400 Bad Request` | `INVALID_INPUT`, `DOMAIN_RULE_VIOLATION` |
| `404 Not Found` | `RESERVATION_NOT_FOUND`, `RESERVATION_TIME_NOT_FOUND`, `THEME_NOT_FOUND` |
| `409 Conflict` | `DOMAIN_CONFLICT`, `DUPLICATE_WAITING`, `DUPLICATE_RESERVATION` |
| `405 Method Not Allowed` | `METHOD_NOT_ALLOWED` |
| `500 Internal Server Error` | `INTERNAL_ERROR` |

## 테스트 구성

| 레이어 | 클래스 | 도구 |
|---|---|---|
| 도메인 | `WaitingTest` | 순수 단위 |
| 서비스 | `WaitingServiceTest` | Mockito |
| 컨트롤러 | `WaitingControllerTest` | `@WebMvcTest` + MockMvc |
| 저장소 | `WaitingRepositoryTest`, `ReservationRepositoryTest` | `@JdbcTest` + H2 |
| 엔드투엔드 | `WaitingE2ETest` | `@SpringBootTest` + RestAssured |
