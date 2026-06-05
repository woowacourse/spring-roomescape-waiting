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

#### ReservationRequest 필드

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| name | String | `@NotBlank` | 예약자 이름 |
| date | LocalDate (`yyyy-MM-dd`) | `@FutureOrPresent` | 예약 날짜 (현재 이후) |
| timeId | Long | `@NotNull` | 예약 시간 ID |
| themeId | Long | `@NotNull` | 테마 ID |

#### UserReservationUpdateRequest (PATCH `/reservations/{id}`)
예약의 날짜·시간·테마를 변경할 때 사용한다. (이름은 변경하지 않으므로 포함하지 않는다.)

```json
{
  "date": "yyyy-MM-dd",
  "timeId": 1,
  "themeId": 1
}
```

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| date | LocalDate (`yyyy-MM-dd`) | `@NotNull`, `@FutureOrPresent` ("예약일은 현재 이후여야 합니다.") | 변경할 날짜 |
| timeId | Long | `@NotNull` | 변경할 시간 ID |
| themeId | Long | `@NotNull` | 변경할 테마 ID |

#### ReservationResponse
```json
{
  "id": 1,
  "name": "string",
  "date": "yyyy-MM-dd",
  "timeResponse": { "id": 1, "startAt": "HH:mm" },
  "themeResponse": { "id": 1, "name": "string", "description": "string", "url": "string" }
}
```

> 예약의 확정/대기 상태는 더 이상 컬럼으로 저장하지 않고, 같은 슬롯의 `requested_at`(요청 시각) 순서로 파생한다.

#### ReservationOrderResponse
`ReservationResponse` 필드에 `order` (대기 순번) 추가. `order == 0`이면 확정, `order >= 1`이면 대기 N번.

```json
{
  "id": 1,
  "name": "string",
  "date": "yyyy-MM-dd",
  "timeResponse": { "id": 1, "startAt": "HH:mm" },
  "themeResponse": { "id": 1, "name": "string", "description": "string", "url": "string" },
  "order": 0
}
```

#### 예약 관련 에러 응답
| 상황 | 상태 코드 | 예외 |
|------|-----------|------|
| 동일인이 같은 슬롯에 중복 예약/대기 | 409 Conflict | `DuplicateReservationException` |
| 존재하지 않는 시간/테마 ID | 404 Not Found | `IdNotFoundException` |
| 지난 날짜·시간 예약, 이미 예약 존재 슬롯으로 변경 | 400 Bad Request | `IllegalArgumentException` |

모든 에러 응답 본문은 `GlobalErrorResponse` 형식: `{ "message": "string" }`

---

### 예약 시간 (ReservationTime)

| 메서드 | URL | 요청 | 응답 | 설명 |
|--------|-----|------|------|------|
| GET | `/times` | - | 200 `ReservationTimeResponse[]` | 예약 시간대 전체 조회 |
| GET | `/times/available-times` | `?themeId={id}&date={yyyy-MM-dd}` | 200 `ReservationTimeStatusResponse[]` | 예약 가능 시간 조회 |

#### ReservationTimeResponse
```json
{ "id": 1, "startAt": "HH:mm" }
```

#### ReservationTimeStatusResponse
```json
{
  "id": 1,
  "startAt": "HH:mm",
  "status": "AVAILABLE | CONFIRMED"
}
```

> `available-times`는 슬롯에 예약이 하나라도 있으면 `CONFIRMED`(점유), 없으면 `AVAILABLE`(예약 가능)을 내려준다. 점유된 시간도 화면에서 선택해 대기 신청할 수 있다.

---

### 테마 (Theme)

| 메서드 | URL | 요청 | 응답 | 설명 |
|--------|-----|------|------|------|
| GET | `/themes` | - | 200 `ThemeResponse[]` | 테마 전체 조회 |
| GET | `/themes/popular` | `?limit={n}` | 200 `ThemeResponse[]` | 예약이 많은 순서대로 n개의 테마 조회 |

#### ThemeResponse
```json
{ "id": 1, "name": "string", "description": "string", "url": "string" }
```

---

### 관리자 (Admin)

| 메서드 | URL | 요청 | 응답 | 설명 |
|--------|-----|------|------|------|
| POST | `/admin/times` | `ReservationTimeRequest` (JSON) | 201 `ReservationTimeResponse` | 예약 시간대 등록 |
| DELETE | `/admin/times/{id}` | - | 204 | 예약 시간대 삭제 |
| POST | `/admin/themes` | `ThemeRequest` (multipart/form-data) | 201 `ThemeResponse` | 테마 등록 |
| DELETE | `/admin/themes/{id}` | - | 204 | 테마 삭제 |

#### ReservationTimeRequest (POST `/admin/times`)
```json
{ "startAt": "HH:mm" }
```

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| startAt | LocalTime (`HH:mm`) | `@NotNull` | 예약 시간대 |

#### ThemeRequest (multipart/form-data)
| 필드 | 타입 | 설명 |
|------|------|------|
| name | String | 테마 이름 (필수) |
| description | String | 테마 설명 (필수) |
| file | MultipartFile | 썸네일 이미지 (필수) |

## 사이클 2 기능 요구사항

### **1단계 - 예약 대기 승인**

-[x] 예약 대기를 승인하여 자동 전환
-[x] 대기가 예약으로 전환되면 해당 슬롯의 나머지 대기 순번이 재정렬
-[x] 예약이 취소되면 해당 슬롯의 대기 순번이 재정렬
