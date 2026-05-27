# 방탈출 예약 시스템
## 기능 목록

#### 1단계 - 예약 대기 신청/취소
- [x] 이미 예약된 슬롯(날짜+시간+테마)에 대기 신청
- [x] 같은 슬롯 대기는 신청 순서대로 순번 부여
- [x] 같은 사용자가 같은 슬롯에 중복 대기 불가
- [x] 본인 대기 취소 (지난 대기 취소 불가, 타인 대기 취소 불가)

#### 2단계 - 내 예약 목록 조회 (상태 구분)
- [x] `GET /reservations?name=` — 예약과 대기를 상태로 구분하여 함께 반환
- [x] 대기 항목에 본인 순번(`rank`) 포함

---
## API 명세

### 예약 시간 (Reservation Time)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `GET` | `/times?date={date}&themeId={id}` | - | `200` |
| 관리자 | `GET` | `/admin/times` | - | `200` |
| 관리자 | `POST` | `/admin/times` | `{"startAt": "HH:mm"}` | `201` |
| 관리자 | `DELETE` | `/admin/times/{id}` | - | `204` |

`GET /times?date=2026-05-01&themeId=1` 응답 예시 (해당 날짜 + 테마에 예약되지 않은 시간만 반환)

```json
[
  { "id": 1, "startAt": "10:00" },
  { "id": 2, "startAt": "14:00" }
]
```

---

### 테마 (Theme)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `GET` | `/themes` | - | `200` |
| 사용자 | `GET` | `/themes/popular?limit={n}` | - | `200` |
| 관리자 | `GET` | `/admin/themes` | - | `200` |
| 관리자 | `POST` | `/admin/themes` | `{"name", "thumbnailUrl", "description"}` | `201` |
| 관리자 | `DELETE` | `/admin/themes/{id}` | - | `204` |

---

### 예약 (Reservation)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `GET` | `/reservations?name={name}` | - | `200` |
| 사용자 | `POST` | `/reservations` | `{"name", "date", "timeId", "themeId"}` | `201` |
| 사용자 | `DELETE` | `/reservations/{id}` | - | `204` |
| 사용자 | `PATCH` | `/reservations/{id}` | `{"date", "timeId"}` | `200` |
| 관리자 | `GET` | `/admin/reservations` | - | `200` |
| 관리자 | `POST` | `/admin/reservations` | `{"name", "date", "timeId", "themeId"}` | `201` |
| 관리자 | `DELETE` | `/admin/reservations/{id}` | - | `204` |

`GET /reservations?name={name}` — 예약과 대기를 함께 반환한다. `status` 필드로 구분하며, 대기는 `rank`(순번)를 포함한다.

```json
[
  {
    "id": 1,
    "name": "홍길동",
    "date": "2026-06-01",
    "time": { "id": 1, "startAt": "10:00" },
    "theme": { "id": 1, "name": "공포의 저택", "thumbnailUrl": "https://...", "description": "..." },
    "status": "예약",
    "rank": null
  },
  {
    "id": 3,
    "name": "홍길동",
    "date": "2026-06-05",
    "time": { "id": 2, "startAt": "14:00" },
    "theme": { "id": 1, "name": "공포의 저택", "thumbnailUrl": "https://...", "description": "..." },
    "status": "예약대기",
    "rank": 2
  }
]
```

예약 생성 요청 예시

```json
{
  "name": "홍길동",
  "date": "2026-05-01",
  "timeId": 1,
  "themeId": 2
}
```

---

### 예약 대기 (Waiting)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `POST` | `/reservations/waitings` | `{"name", "date", "timeId", "themeId"}` | `201` |
| 사용자 | `DELETE` | `/reservations/waitings/{id}?name={name}` | - | `204` |

- 대기 신청은 해당 슬롯에 예약이 존재할 때만 가능하다.
- 예약자 본인은 같은 슬롯에 대기를 신청할 수 없다.
- 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
- 취소는 본인만 가능하며, 이미 시작된 대기는 취소할 수 없다.

대기 신청 요청 예시

```json
{
  "name": "홍길동",
  "date": "2026-06-01",
  "timeId": 1,
  "themeId": 1
}
```

---

## 에러 응답 형식

```json
{ "message": "..." }
```

| 상황 | 코드 |
|------|------|
| 필수 값 누락 / 형식 오류 | `400` |
| 비즈니스 규칙 위반 (지난 예약·대기 취소·변경 시도 등) | `400` |
| 존재하지 않는 FK 참조 (timeId, themeId) | `400` |
| URL로 표현되는 리소스가 존재하지 않음 | `404` |
| 타인의 대기 취소 시도 | `403` |
| 중복 예약 / 중복 대기 | `409` |

---
