# RoomEscape Waiting

## API 명세

### 예약 (Reservation)

| 메서드 | URL | 요청 | 응답 | 설명 |
|--------|-----|------|------|------|
| GET | `/reservations` | - | 200 `ReservationResponse[]` | 전체 예약 조회 |
| GET | `/reservations/my-reservation` | `?name={name}` | 200 `ReservationOrderResponse[]` | 이름 기반 본인 예약 조회 |
| POST | `/reservations` | `ReservationRequest` (JSON) | 201 `ReservationResponse` | 예약 등록 |
| PATCH | `/reservations/{id}` | `UserReservationUpdateRequest` (JSON) | 200 `ReservationResponse` | 예약 수정 |
| DELETE | `/reservations/{id}` | - | 204 | 예약 삭제 |

#### ReservationRequest
```json
{
  "name": "string (필수)",
  "date": "yyyy-MM-dd (현재 이후)",
  "timeId": 1,
  "themeId": 1
}
```

#### ReservationResponse
```json
{
  "id": 1,
  "name": "string",
  "date": "yyyy-MM-dd",
  "timeResponse": { "id": 1, "startAt": "HH:mm" },
  "themeResponse": { "id": 1, "name": "string", "description": "string", "url": "string" },
  "status": "AVAILABLE | WAITING | CONFIRMED"
}
```

#### ReservationOrderResponse
`ReservationResponse` 필드에 `order` (대기 순번) 추가.

---

### 예약 시간 (ReservationTime)

| 메서드 | URL | 요청 | 응답 | 설명 |
|--------|-----|------|------|------|
| GET | `/times` | - | 200 `ReservationTimeResponse[]` | 예약 시간대 전체 조회 |
| GET | `/times/available-times` | `?themeId={id}&date={yyyy-MM-dd}` | 200 `ReservationTimeStatusResponse[]` | 예약 가능 시간 조회 |

#### ReservationTimeStatusResponse
```json
{
  "id": 1,
  "startAt": "HH:mm",
  "status": "AVAILABLE | WAITING | CONFIRMED"
}
```

---

### 테마 (Theme)

| 메서드 | URL | 요청 | 응답 | 설명 |
|--------|-----|------|------|------|
| GET | `/themes` | - | 200 `ThemeResponse[]` | 테마 전체 조회 |
| GET | `/themes/popular` | `?limit={n}` | 200 `ThemeResponse[]` | 예약이 많은 순서대로 n개의 테마 조회 |

---

### 관리자 (Admin)

| 메서드 | URL | 요청 | 응답 | 설명 |
|--------|-----|------|------|------|
| POST | `/admin/times` | `ReservationTimeRequest` (JSON) | 201 `ReservationTimeResponse` | 예약 시간대 등록 |
| DELETE | `/admin/times/{id}` | - | 204 | 예약 시간대 삭제 |
| POST | `/admin/themes` | `ThemeRequest` (multipart/form-data) | 201 `ThemeResponse` | 테마 등록 |
| DELETE | `/admin/themes/{id}` | - | 204 | 테마 삭제 |

#### ThemeRequest (multipart/form-data)
| 필드 | 타입 | 설명 |
|------|------|------|
| name | String | 테마 이름 (필수) |
| description | String | 테마 설명 (필수) |
| file | MultipartFile | 썸네일 이미지 (필수) |
