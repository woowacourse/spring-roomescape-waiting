# 방탈출 예약 대기

## 기능 요구 사항

방탈출 사용자 예약 미션까지는 한 슬롯(날짜+시간+테마)에 한 명만 예약할 수 있었고, 이미 예약된 슬롯은 사용자에게 보이지 않았다.
이번 사이클부터는 이미 예약된 슬롯에 대기를 신청할 수 있고, 사용자는 본인의 예약과 대기를 함께 조회할 수 있다.

이번 사이클의 작업도 백엔드 API 추가와 사용자가 보는 화면을 만드는 것 두 가지를 함께 진행한다.
API에 맞춰 페어가 함께 사용자가 사용하는 클라이언트 화면을 만들고, 각 단계의 화면이 브라우저에서 정상 동작하는 것까지 확인한다. 화면 작성에는 AI를 활용해도 좋다.

## 프론트엔드 레포지토리
https://github.com/Uechann/react-roomscape

### 1단계 - 예약 대기 신청/취소

이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.

같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.

같은 사용자가 같은 슬롯에 중복 대기할 수 없다.

사용자는 본인의 대기를 취소할 수 있다.

#### 예약 대기 신청

```
POST /reservations
```

**요청 Body**

```json
{
  "name": "브라운",
  "themeSlotId": 1
}
```

| 필드       | 타입   | 필수 | 설명                         |
|------------|--------|------|------------------------------|
| name       | String | O    | 예약자 이름 (공백 불가)       |
| themeSlotId | Long  | O    | 예약 슬롯 ID (테마+날짜+시간) |

> `themeSlotId`는 `GET /times?themeId={themeId}&date={date}` 응답의 `id` 값이다.

**응답 201 Created** — 등록된 예약 단건 (전체 예약 조회 요소와 동일한 형식)


| 에러 상황                    | 상태 코드                |
|------------------------------|--------------------------|
| 유효하지 않은 입력값         | 400 Bad Request          |
| 슬롯 ID 없음                 | 404 Not Found            |
| 과거 날짜 예약 시도          | 422 Unprocessable Entity |
| 당일 이미 지난 시간으로 예약 | 422 Unprocessable Entity |
| 중복 사용자 슬롯 예약/대기   | 409 Conflict             |

### 예약 취소

```
PATCH /reservations/{reservationId}/cancel
```


| 경로 변수     | 타입 | 설명    |
|---------------|------|---------|
| reservationId | Long | 예약 ID |

**응답 204 No Content**


| 에러 상황                                                  | 상태 코드                |
|------------------------------------------------------------|--------------------------|
| 예약 없음                                                  | 404 Not Found            |
| 취소할 수 없는 예약 (이미 취소되었거나, 완료 처리 된 예약) | 422 Unprocessable Entity |

---

### 예약 플로우

취소하고자 하는 예약이 PENDING이라면

- [X]  cancel로 상태 변경

최소하고자 하는 예약이 CONFIRMNED이라면

- [X]  cancel로 상태 변경

- 이때 만약 대기하고 있는 PENDING 예약이 존재한다면
  - [X]  대기중인 PENDING 예약을 CONFIRM으로 변경하고 업데이트
- 대기하고 있는 PENDING 예약이 없다면
  - [X]  themeSlot을 false로 변경후 업데이트 한다.

취소하고자 하는 예약이 COMPLETE, CANCELLED라면

- [x]  취소할 수 없다는 예외처리

### 상태 변환 흐름

![img_1.png](img_1.png)

### 2단계 - 내 예약 목록 조회 (상태 구분)

이전 미션의 내 예약 목록 조회를 확장한다.
사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
대기에는 본인의 대기 순번도 함께 보여준다.



#### 내 예약 조회

```
GET /reservations?name={name}
```

| 파라미터 | 타입   | 필수 | 설명        |
|----------|--------|------|-------------|
| name     | String | O    | 예약자 이름 |

**응답 200 OK**
```json
{
  "reservationResponses": [
    {
      "id": 1,
      "name": "브라운",
      "date": "2026-05-15",
      "time": {
        "id": 1,
        "startAt": "10:00",
        "isAvailable": true
      },
      "theme": {
        "id": 1,
        "name": "공포의 방",
        "description": "무서운 방탈출",
        "thumbnailUrl": "https://example.com/thumbnail.jpg"
      },
      "status": "CONFIRMED"
    }
  ],
  "waitingReservationResponses": [
    {
      "id": 2,
      "name": "브라운",
      "date": "2026-05-15",
      "time": {
        "id": 1,
        "startAt": "10:00",
        "isAvailable": true
      },
      "theme": {
        "id": 1,
        "name": "공포의 방",
        "description": "무서운 방탈출",
        "thumbnailUrl": "https://example.com/thumbnail.jpg"
      },
      "status": "PENDING",
      "waitingOrder": 1
    }
  ]
}
```

> `reservationResponses`에는 PENDING이 아닌 예약(CONFIRMED, COMPLETED, CANCELLED)이 포함된다.
> `waitingReservationResponses`에는 PENDING 상태의 대기 예약이 포함되며, `waitingOrder`는 해당 슬롯에서의 대기 순번이다.


### 중요 로직 플로우

```mermaid
flowchart TD
    A[예약 취소 요청] --> B[ReservationService.cancelReservation 시작]
    B --> TX[트랜잭션 시작]

    TX --> C[취소 대상 Reservation 조회]
    C --> D{취소 대상 상태}

    D -- PENDING --> E[Reservation 도메인: cancel]
    E --> F[reservation.status = CANCELLED 업데이트]
    F --> G[theme_slot 변경 없음]
    G --> COMMIT[트랜잭션 커밋]

    D -- CONFIRMED --> H[해당 ThemeSlot row 잠금]
    H --> I[Reservation 도메인: cancel]
    I --> J[reservation.status = CANCELLED 업데이트]

    J --> K[같은 theme/date/time의 PENDING 예약 조회]
    K --> L[ORDER BY created_at ASC, id ASC LIMIT 1]

    L --> M{가장 빠른 PENDING 존재?}

    M -- 예 --> N[Pending Reservation 도메인: confirm]
    N --> O[reservation.status = CONFIRMED 업데이트]
    O --> P[theme_slot.is_reserved = true 유지]
    P --> COMMIT

    M -- 아니오 --> Q[theme_slot.is_reserved = false 업데이트]
    Q --> COMMIT

    COMMIT --> R[204 No Content 응답]
```

```mermaid
flowchart TD
    A[동시 예약 요청 A, B 수신] --> B[요청 A INSERT]
    A --> C[요청 B INSERT]

    B --> D[DB가 created_at, id 부여]
    C --> D

    D --> E[예약 대기 목록 조회]

    E --> F[ORDER BY created_at ASC, id ASC]

    F --> G{created_at이 같은가?}

    G -- 아니오 --> H[created_at이 빠른 예약이 먼저]
    G -- 예 --> I[id가 작은 예약이 먼저]

    H --> J[최종 대기 순서 확정]
    I --> J
```

---

## API 명세서

### 공통 응답 형식

#### ReservationResponse

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-15",
  "time": {
    "id": 1,
    "startAt": "10:00:00",
    "isAvailable": true
  },
  "theme": {
    "id": 1,
    "name": "폐병원 탈출",
    "description": "버려진 병원에서 벌어지는 기괴한 일들.",
    "thumbnailUrl": "https://example.com/thumbnail.jpg"
  },
  "status": "CONFIRMED"
}
```

#### WaitingReservationResponse

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-15",
  "time": {
    "id": 1,
    "startAt": "10:00:00",
    "isAvailable": true
  },
  "theme": {
    "id": 1,
    "name": "폐병원 탈출",
    "description": "버려진 병원에서 벌어지는 기괴한 일들.",
    "thumbnailUrl": "https://example.com/thumbnail.jpg"
  },
  "status": "PENDING",
  "waitingOrder": 1
}
```

#### ThemeResponse

```json
{
  "id": 1,
  "name": "폐병원 탈출",
  "description": "버려진 병원에서 벌어지는 기괴한 일들.",
  "thumbnailUrl": "https://example.com/thumbnail.jpg"
}
```

#### TimeResponse

```json
{
  "id": 1,
  "startAt": "10:00:00",
  "isAvailable": true
}
```

---

### 예약 API

#### 전체 예약 목록 조회

```
GET /reservations
```

**응답 200 OK**

```json
[
  {
    "id": 1,
    "name": "브라운",
    "date": "2026-05-15",
    "time": { "id": 1, "startAt": "10:00:00", "isAvailable": true },
    "theme": { "id": 1, "name": "폐병원 탈출", "description": "...", "thumbnailUrl": "..." },
    "status": "CONFIRMED"
  }
]
```

---

#### 내 예약 목록 조회

```
GET /reservations?name={name}
```

| 쿼리 파라미터 | 타입   | 필수 | 설명        |
|--------------|--------|------|-------------|
| name         | String | O    | 예약자 이름 |

**응답 200 OK**

```json
{
  "reservationResponses": [
    {
      "id": 1,
      "name": "브라운",
      "date": "2026-05-15",
      "time": { "id": 1, "startAt": "10:00:00", "isAvailable": true },
      "theme": { "id": 1, "name": "폐병원 탈출", "description": "...", "thumbnailUrl": "..." },
      "status": "CONFIRMED"
    }
  ],
  "waitingReservationResponses": [
    {
      "id": 2,
      "name": "브라운",
      "date": "2026-05-16",
      "time": { "id": 1, "startAt": "10:00:00", "isAvailable": true },
      "theme": { "id": 1, "name": "폐병원 탈출", "description": "...", "thumbnailUrl": "..." },
      "status": "PENDING",
      "waitingOrder": 1
    }
  ]
}
```

> `reservationResponses`: PENDING이 아닌 예약 목록 (CONFIRMED, COMPLETED, CANCELLED)
> `waitingReservationResponses`: PENDING 상태의 대기 목록 (슬롯 내 대기 순번 포함)

| 에러 상황        | 상태 코드       |
|------------------|----------------|
| name이 빈 문자열 | 400 Bad Request |

---

#### 예약 생성 (대기 신청 포함)

```
POST /reservations
```

**요청 Body**

```json
{
  "name": "브라운",
  "themeSlotId": 1
}
```

| 필드        | 타입   | 필수 | 설명                              |
|-------------|--------|------|-----------------------------------|
| name        | String | O    | 예약자 이름 (공백 불가)            |
| themeSlotId | Long   | O    | 예약 슬롯 ID (`GET /times?themeId&date` 응답의 `id`) |

**응답 201 Created** — ReservationResponse 단건

- 슬롯에 기존 예약이 없으면 `CONFIRMED` 상태로 생성
- 슬롯에 기존 예약이 있으면 `PENDING` 상태(대기)로 생성

| 에러 상황                              | 상태 코드                |
|----------------------------------------|--------------------------|
| 필수 필드 누락 또는 빈 값              | 400 Bad Request          |
| 슬롯 ID 존재하지 않음                  | 404 Not Found            |
| 과거 날짜로 예약 시도                  | 422 Unprocessable Entity |
| 당일 이미 지난 시간으로 예약 시도      | 422 Unprocessable Entity |
| 같은 사용자가 동일 슬롯에 중복 예약/대기 | 409 Conflict           |

---

#### 예약 취소

```
PATCH /reservations/{reservationId}/cancel
```

| 경로 변수     | 타입 | 설명    |
|--------------|------|---------|
| reservationId | Long | 예약 ID |

**응답 204 No Content**

- CONFIRMED 예약 취소 시, 같은 슬롯의 첫 번째 PENDING 예약이 자동으로 CONFIRMED로 승격
- PENDING 예약 취소 시, 단순 CANCELLED 처리

| 에러 상황                  | 상태 코드                |
|----------------------------|--------------------------|
| 예약 ID 존재하지 않음      | 404 Not Found            |
| 이미 취소된 예약           | 422 Unprocessable Entity |
| 이미 완료된 예약           | 422 Unprocessable Entity |

---

#### 예약 수정

```
PATCH /reservations/{reservationId}
```

| 경로 변수     | 타입 | 설명    |
|--------------|------|---------|
| reservationId | Long | 예약 ID |

**요청 Body**

```json
{
  "themeSlotId": 2
}
```

| 필드        | 타입 | 필수 | 설명                    |
|-------------|------|------|-------------------------|
| themeSlotId | Long | O    | 변경할 예약 슬롯 ID      |

**응답 200 OK** — 수정된 ReservationResponse 단건

| 에러 상황                     | 상태 코드                |
|-------------------------------|--------------------------|
| 예약 ID 존재하지 않음         | 404 Not Found            |
| 슬롯 ID 존재하지 않음         | 404 Not Found            |
| 과거 날짜로 변경 시도         | 422 Unprocessable Entity |
| 당일 이미 지난 시간으로 변경  | 422 Unprocessable Entity |
| 변경할 슬롯에 이미 예약 존재  | 409 Conflict             |

---

#### 예약 삭제

```
DELETE /reservations/{id}
```

| 경로 변수 | 타입 | 설명    |
|----------|------|---------|
| id       | Long | 예약 ID |

**응답 204 No Content**

| 에러 상황             | 상태 코드     |
|-----------------------|---------------|
| 예약 ID 존재하지 않음 | 404 Not Found |

---

### 테마 API

#### 전체 테마 목록 조회

```
GET /themes
```

**응답 200 OK**

```json
[
  {
    "id": 1,
    "name": "폐병원 탈출",
    "description": "버려진 병원에서 벌어지는 기괴한 일들.",
    "thumbnailUrl": "https://example.com/thumbnail.jpg"
  }
]
```

---

#### 인기 테마 조회

```
GET /themes?topCount={topCount}&during={during}
```

| 쿼리 파라미터 | 타입 | 필수 | 설명                            |
|--------------|------|------|---------------------------------|
| topCount     | Long | O    | 조회할 테마 수                   |
| during       | Long | O    | 최근 N일 기준 (오늘 포함 N일 전) |

최근 `during`일 동안 예약 수가 가장 많은 테마를 `topCount`개 순서대로 반환한다.

**응답 200 OK** — ThemeResponse 배열 (예약 수 내림차순)

```json
[
  {
    "id": 1,
    "name": "폐병원 탈출",
    "description": "버려진 병원에서 벌어지는 기괴한 일들.",
    "thumbnailUrl": "https://example.com/thumbnail.jpg"
  }
]
```

---

#### 테마 생성

```
POST /themes
```

**요청 Body**

```json
{
  "name": "새로운 테마",
  "description": "테마 설명",
  "thumbnailUrl": "https://example.com/thumbnail.jpg"
}
```

| 필드         | 타입   | 필수 | 설명                  |
|--------------|--------|------|-----------------------|
| name         | String | O    | 테마 이름 (공백 불가) |
| description  | String | O    | 테마 설명 (공백 불가) |
| thumbnailUrl | String | O    | 썸네일 URL (공백 불가) |

**응답 201 Created** — ThemeResponse 단건

| 에러 상황                    | 상태 코드       |
|-----------------------------|----------------|
| 필수 필드 누락 또는 빈 값    | 400 Bad Request |

---

#### 테마 삭제

```
DELETE /themes/{id}
```

| 경로 변수 | 타입 | 설명    |
|----------|------|---------|
| id       | Long | 테마 ID |

**응답 204 No Content**

| 에러 상황                       | 상태 코드                |
|---------------------------------|--------------------------|
| 테마 ID 존재하지 않음           | 404 Not Found            |
| 해당 테마를 참조하는 예약 존재  | 422 Unprocessable Entity |

---

### 시간 API

#### 전체 시간 목록 조회

```
GET /times
```

**응답 200 OK**

```json
[
  {
    "id": 1,
    "startAt": "10:00:00",
    "isAvailable": true
  }
]
```

> `isAvailable`은 전체 조회 시 항상 `true`로 반환된다. 특정 날짜·테마 기준 예약 가능 여부는 아래 엔드포인트를 사용한다.

---

#### 테마·날짜 기준 시간 조회 (예약 가능 여부 포함)

```
GET /times?themeId={themeId}&date={date}
```

| 쿼리 파라미터 | 타입      | 필수 | 설명                     |
|--------------|-----------|------|--------------------------|
| themeId      | Long      | O    | 테마 ID                   |
| date         | LocalDate | O    | 조회 날짜 (`yyyy-MM-dd`) |

해당 테마·날짜에 대한 슬롯이 없으면 자동으로 생성 후 반환한다.

**응답 200 OK**

```json
[
  {
    "id": 10,
    "startAt": "10:00:00",
    "isAvailable": true
  },
  {
    "id": 11,
    "startAt": "14:00:00",
    "isAvailable": false
  }
]
```

> `id`는 `theme_slot` 테이블의 ID이다. 예약 생성(`POST /reservations`)의 `themeSlotId`에 이 값을 사용한다.
> `isAvailable`이 `false`이면 해당 슬롯은 이미 예약된 상태이므로 대기 신청만 가능하다.

| 에러 상황              | 상태 코드     |
|-----------------------|---------------|
| 테마 ID 존재하지 않음  | 404 Not Found |

---

#### 시간 생성

```
POST /times
```

**요청 Body**

```json
{
  "startAt": "10:00:00"
}
```

| 필드    | 타입      | 필수 | 설명                               |
|---------|-----------|------|------------------------------------|
| startAt | LocalTime | O    | 시작 시간 (`HH:mm` 또는 `HH:mm:ss`) |

**응답 201 Created** — TimeResponse 단건

| 에러 상황      | 상태 코드       |
|---------------|----------------|
| startAt 누락  | 400 Bad Request |

---

#### 시간 삭제

```
DELETE /times/{id}
```

| 경로 변수 | 타입 | 설명    |
|----------|------|---------|
| id       | Long | 시간 ID |

**응답 204 No Content**

| 에러 상황                       | 상태 코드                |
|---------------------------------|--------------------------|
| 시간 ID 존재하지 않음           | 404 Not Found            |
| 해당 시간을 참조하는 예약 존재  | 422 Unprocessable Entity |