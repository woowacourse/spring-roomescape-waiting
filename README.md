# 방탈출 예약 대기 (Room Escape Waiting)

## 예약 대기 승인

### 변경 요구사항

- 예약 대기를 승인하여 예약으로 전환
- 대기가 예약으로 전환되면 해당 슬롯의 나머지 대기 순번이 재정렬된다.
- 예약이 취소되면 해당 슬롯의 대기 순번이 재정렬된다.
- 요구사항에 명시되지 않은 엣지 케이스를 스스로 식별하고 처리한다.

**추가된 요구사항**

토론에서 정한 트랜잭션 경계에 맞춰 함께 일어나야 하는 데이터 변경을 묶고, 중간 실패 시 데이터 일관성이 유지되는지 테스트로 확인한다.

---

## 기능 목록

### 예약 취소 시 대기 자동 승격 (이번 사이클)

- [x] 예약을 취소하면 해당 슬롯의 첫 번째 대기자를 예약으로 자동 승격한다.
  - [x] 대기자가 없으면 아무도 승격하지 않고 취소만 수행한다.
  - [x] 승격된 대기는 대기 목록에서 제거된다.
- [x] 대기 승격 / 예약 취소 시 해당 슬롯의 남은 대기 순번이 재정렬된다. (순번은 `id` 기준으로 동적 계산)
- [x] 예약 취소와 대기 승격을 하나의 트랜잭션으로 묶어 원자적으로 처리한다.
- [x] 동시 취소 시 한 대기자가 중복 승격되지 않도록 첫 대기자 조회에 `FOR UPDATE` 락을 적용한다.

### 엣지 케이스 처리

- [x] 본인 예약이 아니면 취소할 수 없다. (`DomainConflictException`)
- [x] 존재하지 않는 예약을 취소하면 `RESERVATION_NOT_FOUND` 예외를 던진다.
- [x] 예약이 없는 슬롯에는 대기를 생성할 수 없다. (`WAITING_WITHOUT_RESERVATION`)
- [x] 이미 예약한 사람은 같은 슬롯에 대기를 걸 수 없다. (`DUPLICATE_RESERVATION`)
- [x] 같은 슬롯에 같은 사람이 중복 대기할 수 없다. (`DUPLICATE_WAITING`)

---

## API 명세

> 검증 실패/도메인 규칙 위반 시에는 `{ "code": "에러 식별 코드", "message": "에러 메시지" }` 형식의 본문이 내려간다.

---

### GET `/reservations` — 내 예약 목록 조회

`name`으로 지정한 사용자의 예약 목록을 페이지 단위로 조회한다.

**Request — Query Parameter**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `name` | String | O | - | 조회 대상자의 이름. 해당 이름의 예약만 반환한다. |
| `page` | Number | X | `0` | 페이지 번호 (0부터 시작) |
| `size` | Number | X | `10` | 페이지당 항목 수 |

**Response — `200 OK`**

```json
{
  "reservations": [
    {
      "id": 1,
      "name": "레서",
      "date": "2026-06-07",
      "time": { "id": 1, "startAt": "10:00" },
      "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" }
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `reservations` | Array | 예약 항목 배열. 없으면 빈 배열(`[]`). |
| `id` | Number | 예약 식별자 |
| `name` | String | 예약자 이름 |
| `date` | String (`yyyy-MM-dd`) | 예약 날짜 |
| `time` | Object | 예약 시간. `id`(시간 식별자), `startAt`(시작 시각, `HH:mm`)으로 구성된다. |
| `theme` | Object | 예약 테마. `id`(테마 식별자), `name`(이름), `description`(설명), `thumbnail`(썸네일 URL)으로 구성된다. |

---

### POST `/reservations` — 예약 생성

지정한 날짜/시간/테마 슬롯에 예약을 생성한다.

**Request — Body**

```json
{
  "name": "레서",
  "date": "2026-06-07",
  "timeId": 1,
  "themeId": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `name` | String | O | 예약자 이름. 비어 있을 수 없다. |
| `date` | String (`yyyy-MM-dd`) | O | 예약 날짜 |
| `timeId` | Number | O | 예약할 시간의 식별자 |
| `themeId` | Number | O | 예약할 테마의 식별자 |

**Response — `201 Created`**

```json
{
  "id": 1,
  "name": "레서",
  "date": "2026-06-07",
  "time": { "id": 1, "startAt": "10:00" },
  "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Number | 생성된 예약 식별자 |
| `name` | String | 예약자 이름 |
| `date` | String (`yyyy-MM-dd`) | 예약 날짜 |
| `time` | Object | 예약 시간. `id`(시간 식별자), `startAt`(시작 시각, `HH:mm`)으로 구성된다. |
| `theme` | Object | 예약 테마. `id`(테마 식별자), `name`(이름), `description`(설명), `thumbnail`(썸네일 URL)으로 구성된다. |

---

### PATCH `/reservations/{id}` — 예약 변경

기존 예약의 날짜/시간을 변경한다. (테마는 변경 대상이 아니다.)

**Request — Path / Query Parameter**

| 위치 | 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Path | `id` | Number | O | 변경할 예약의 식별자 |
| Query | `name` | String | O | 요청자 이름. 본인 예약만 변경할 수 있다. |

**Request — Body**

```json
{
  "date": "2026-06-08",
  "timeId": 2
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `date` | String (`yyyy-MM-dd`) | O | 변경할 예약 날짜 |
| `timeId` | Number | O | 변경할 시간의 식별자 |

**Response — `200 OK`**

```json
{
  "id": 1,
  "name": "레서",
  "date": "2026-06-08",
  "time": { "id": 2, "startAt": "12:00" },
  "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Number | 예약 식별자 |
| `name` | String | 예약자 이름 |
| `date` | String (`yyyy-MM-dd`) | 변경된 예약 날짜 |
| `time` | Object | 변경된 예약 시간. `id`(시간 식별자), `startAt`(시작 시각, `HH:mm`)으로 구성된다. |
| `theme` | Object | 예약 테마. `id`(테마 식별자), `name`(이름), `description`(설명), `thumbnail`(썸네일 URL)으로 구성된다. |

---

### DELETE `/reservations/{id}` — 예약 취소

예약을 취소한다. 취소되면 같은 슬롯의 첫 번째 대기자가 예약으로 자동 승격된다.

**Request — Path / Query Parameter**

| 위치 | 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Path | `id` | Number | O | 취소할 예약의 식별자 |
| Query | `name` | String | O | 요청자 이름. 본인 예약만 취소할 수 있다. |

**Response — `204 No Content`** — 본문 없음.

---

### GET `/waitings` — 내 예약 대기 목록 조회

`name`으로 지정한 사용자의 대기 목록과 각 대기의 순번을 조회한다.

**Request — Query Parameter**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `name` | String | O | - | 조회 대상자의 이름. 해당 이름의 대기만 반환한다. |
| `page` | Number | X | `0` | 페이지 번호 (0부터 시작) |
| `size` | Number | X | `10` | 페이지당 항목 수 |

**Response — `200 OK`**

```json
{
  "waitingList": [
    {
      "id": 2,
      "name": "레서",
      "date": "2026-06-07",
      "time": { "id": 1, "startAt": "10:00" },
      "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" },
      "order": 2
    }
  ]
}
```

| 필드 | 타입 | 설명                                                                                   |
|---|---|--------------------------------------------------------------------------------------|
| `waitingList` | Array | 대기 항목 배열. 없으면 빈 배열(`[]`).                                                            |
| `id` | Number | 대기 식별자                                                                               |
| `name` | String | 대기자 이름                                                                               |
| `date` | String (`yyyy-MM-dd`) | 대기 슬롯의 날짜                                                                            |
| `time` | Object | 대기 슬롯의 시간. `id`(시간 식별자), `startAt`(시작 시각, `HH:mm`)으로 구성된다.                           |
| `theme` | Object | 대기 슬롯의 테마. `id`(테마 식별자), `name`(이름), `description`(설명), `thumbnail`(썸네일 URL)으로 구성된다. |
| `order` | Number | 같은 슬롯(날짜/시간/테마) 내 대기 순번 (1부터). 아래 설명 참고.                                             |

> `order`는 같은 슬롯 내 `id`가 자신보다 작은 대기 수 + 1로 동적 계산한다.
> 따라서 앞선 대기가 승격/취소되면 별도 갱신 없이 재조회 시 자동으로 당겨진다.

---

### POST `/waitings` — 예약 대기 생성

이미 예약이 존재하는 슬롯에 대기를 건다.

**Request — Body**

```json
{
  "name": "레서",
  "date": "2026-06-07",
  "timeId": 1,
  "themeId": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `name` | String | O | 대기자 이름. 비어 있을 수 없다. |
| `date` | String (`yyyy-MM-dd`) | O | 대기할 슬롯의 날짜 |
| `timeId` | Number | O | 대기할 시간의 식별자 |
| `themeId` | Number | O | 대기할 테마의 식별자 |

**Response — `201 Created`**

```json
{
  "id": 1,
  "name": "레서",
  "date": "2026-06-07",
  "time": { "id": 1, "startAt": "10:00" },
  "theme": { "id": 1, "name": "공포", "description": "무서운 테마", "thumbnail": "thumb.png" },
  "order": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Number | 생성된 대기 식별자 |
| `name` | String | 대기자 이름 |
| `date` | String (`yyyy-MM-dd`) | 대기 슬롯의 날짜 |
| `time` | Object | 대기 슬롯의 시간. `id`(시간 식별자), `startAt`(시작 시각, `HH:mm`)으로 구성된다. |
| `theme` | Object | 대기 슬롯의 테마. `id`(테마 식별자), `name`(이름), `description`(설명), `thumbnail`(썸네일 URL)으로 구성된다. |
| `order` | Number | 생성 시점의 대기 순번 (1부터) |

---

### DELETE `/waitings/{id}` — 예약 대기 취소

**Request — Path / Query Parameter**

| 위치 | 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|---|
| Path | `id` | Number | O | 취소할 대기의 식별자 |
| Query | `name` | String | O | 요청자 이름. 본인 대기만 취소할 수 있다. |

**Response — `204 No Content`** — 본문 없음.
