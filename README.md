# 방탈출 예약 시스템
## 기능 목록

#### 예약 대기 자동 승격
- [x] 예약 제거 시 같은 슬롯의 대기 1번을 자동으로 예약 전환
- [x] 전환된 대기를 대기 목록에서 제거
- [x] 남은 대기 순번은 조회 시점에 동적으로 재계산
- [x] 사용자 본인 취소(`DELETE /reservations/{id}`)에 자동 승격 적용
- [x] 관리자 삭제(`DELETE /admin/reservations/{id}`)에 자동 승격 적용
- [x] 예약 삭제 + 대기 삭제 + 승격 예약 삽입을 하나의 트랜잭션으로 처리

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

예약 삭제/취소 시 같은 날짜·시간·테마 슬롯에 예약 대기가 있으면 가장 먼저 신청한 대기 1명이 자동으로 예약 전환된다. 대기 순번은 저장하지 않고 조회 시점에 계산하므로, 전환된 대기가 제거되면 남은 대기는 다음 조회부터 한 칸씩 당겨진 순번으로 보인다.

트랜잭션 경계: 사용자가 보는 상태가 원자적이어야 하므로 예약 삭제, 대기 1번 삭제, 승격 예약 삽입을 같은 작업 단위로 묶는다. 순번 재정렬은 별도 저장 작업이 아니라 조회 시점 계산이므로 트랜잭션에 묶을 대상이 없다.

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
