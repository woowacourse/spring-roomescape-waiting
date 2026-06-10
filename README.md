# Roomescape Waiting

방탈출 테마, 예약 시간, 예약 및 대기 예약을 관리하는 Spring Boot 애플리케이션입니다.

## 기능 사항

### 사용자 화면

- 예약 페이지를 조회할 수 있다.
- 테마 목록과 최근 인기 테마 목록을 조회할 수 있다.
- 날짜와 테마를 기준으로 예약 가능한 시간을 조회할 수 있다.
- 예약자 이름으로 본인의 예약 및 대기 예약 목록을 조회할 수 있다.

### 예약

- 예약자 이름, 날짜, 시간, 테마를 입력해 예약을 생성할 수 있다.
- 같은 날짜, 시간, 테마에 확정 예약이 없으면 확정 예약으로 등록된다.
- 같은 날짜, 시간, 테마에 확정 예약이 있으면 대기 예약으로 등록된다.
- 같은 예약자가 같은 날짜, 시간, 테마에 중복 예약할 수 없다.
- 예약자는 본인의 확정 예약 또는 대기 예약을 취소할 수 있다.
- 확정 예약이 취소되면 가장 먼저 등록된 대기 예약이 확정 예약으로 승격된다.
- 예약자는 본인의 예약 날짜, 시간, 테마를 변경할 수 있다.
- 확정 예약이 다른 시간대로 변경되면 기존 시간대의 가장 빠른 대기 예약이 확정 예약으로 승격된다.
- 대기 예약은 본인 목록 조회 시 대기 순번을 함께 확인할 수 있다.

### 관리자

- 관리자 페이지를 조회할 수 있다.
- 전체 확정 예약 목록을 페이징 조회할 수 있다.
- 테마를 생성할 수 있다.
- 예약이 존재하지 않는 테마를 비활성화할 수 있다.
- 예약 시간을 생성할 수 있다.
- 확정 예약이 존재하지 않는 예약 시간을 비활성화할 수 있다.

## 화면

- `GET /`, `GET /reservation`: 사용자 예약 화면
- `GET /admin`: 관리자 화면

## API 명세

### 예약

#### 이름으로 예약 조회

```http
GET /reservations?username={username}
```

응답 `200 OK`

```json
[
  {
    "id": 1,
    "name": "포비",
    "date": "2026-05-27",
    "time": {
      "id": 1,
      "startAt": "10:00:00"
    },
    "theme": {
      "id": 1,
      "name": "미드나잇 시그널",
      "thumbnailImageUrl": "https://example.com/theme.jpg",
      "description": "심야 라디오에서 시작되는 미스터리",
      "durationTime": "01:00:00"
    },
    "status": "ACTIVE",
    "pendingOrder": null
  }
]
```

#### 예약 생성

```http
POST /reservations
Content-Type: application/json
```

요청

```json
{
  "name": "포비",
  "date": "2026-05-27",
  "timeId": 1,
  "themeId": 1
}
```

응답 `201 Created`

```json
{
  "id": 1,
  "name": "포비",
  "date": "2026-05-27",
  "time": {
    "id": 1,
    "startAt": "10:00:00"
  },
  "theme": {
    "id": 1,
    "name": "미드나잇 시그널",
    "thumbnailImageUrl": "https://example.com/theme.jpg",
    "description": "심야 라디오에서 시작되는 미스터리",
    "durationTime": "01:00:00"
  },
  "status": "ACTIVE"
}
```

이미 같은 날짜, 시간, 테마에 확정 예약이 있으면 중복 대기 예약이 아닌 경우 대기 예약으로 생성됩니다.

#### 예약 취소

```http
DELETE /reservations/{id}?username={username}
```

응답 `204 No Content`

예약자가 본인의 확정 또는 대기 예약을 취소합니다. 확정 예약이 취소되면 다음 대기 예약이 확정 상태로 변경됩니다.

#### 확정 예약으로 변경

```http
PATCH /reservations/{id}/active
Content-Type: application/json
```

요청

```json
{
  "username": "포비",
  "date": "2026-05-28",
  "timeId": 2,
  "themeId": 1
}
```

응답 `200 OK`

```json
{
  "id": 1,
  "name": "포비",
  "date": "2026-05-28",
  "time": {
    "id": 2,
    "startAt": "11:00:00"
  },
  "theme": {
    "id": 1,
    "name": "미드나잇 시그널",
    "thumbnailImageUrl": "https://example.com/theme.jpg",
    "description": "심야 라디오에서 시작되는 미스터리",
    "durationTime": "01:00:00"
  },
  "status": "ACTIVE"
}
```

#### 대기 예약으로 변경

```http
PATCH /reservations/{id}/pending
Content-Type: application/json
```

요청

```json
{
  "username": "포비",
  "date": "2026-05-28",
  "timeId": 2,
  "themeId": 1
}
```

응답 `200 OK`

```json
{
  "id": 1,
  "name": "포비",
  "date": "2026-05-28",
  "time": {
    "id": 2,
    "startAt": "11:00:00"
  },
  "theme": {
    "id": 1,
    "name": "미드나잇 시그널",
    "thumbnailImageUrl": "https://example.com/theme.jpg",
    "description": "심야 라디오에서 시작되는 미스터리",
    "durationTime": "01:00:00"
  },
  "status": "PENDING"
}
```

선택한 날짜, 시간, 테마에 확정 예약이 있어야 대기 상태로 변경할 수 있습니다.

### 관리자 예약

#### 전체 예약 조회

```http
GET /admin/reservations
```

응답 `200 OK`

```json
[
  {
    "id": 1,
    "name": "포비",
    "date": "2026-05-27",
    "time": {
      "id": 1,
      "startAt": "10:00:00"
    },
    "theme": {
      "id": 1,
      "name": "미드나잇 시그널",
      "thumbnailImageUrl": "https://example.com/theme.jpg",
      "description": "심야 라디오에서 시작되는 미스터리",
      "durationTime": "01:00:00"
    },
    "status": "ACTIVE"
  }
]
```

#### 예약 삭제

```http
DELETE /admin/reservations/{id}
```

응답 `204 No Content`

관리자가 예약을 물리 삭제합니다.

### 예약 시간

#### 전체 예약 시간 조회

```http
GET /times
```

응답 `200 OK`

```json
[
  {
    "id": 1,
    "startAt": "10:00:00"
  }
]
```

#### 예약 가능한 시간 조회

```http
GET /times/available?themeId={themeId}&date={yyyy-MM-dd}
```

응답 `200 OK`

```json
{
  "theme": {
    "id": 1,
    "name": "미드나잇 시그널",
    "thumbnailImageUrl": "https://example.com/theme.jpg",
    "description": "심야 라디오에서 시작되는 미스터리",
    "durationTime": "01:00:00"
  },
  "times": [
    {
      "id": 1,
      "startAt": "10:00:00"
    }
  ]
}
```

#### 예약 시간 생성

```http
POST /admin/times
Content-Type: application/json
```

요청

```json
{
  "startAt": "10:00"
}
```

응답 `201 Created`

```json
{
  "id": 1,
  "startAt": "10:00:00"
}
```

#### 예약 시간 삭제

```http
DELETE /admin/times/{id}
```

응답 `204 No Content`

예약에 사용 중인 시간은 삭제할 수 없습니다.

### 테마

#### 전체 테마 조회

```http
GET /themes
```

응답 `200 OK`

```json
[
  {
    "id": 1,
    "name": "미드나잇 시그널",
    "thumbnailImageUrl": "https://example.com/theme.jpg",
    "description": "심야 라디오에서 시작되는 미스터리",
    "durationTime": "01:00:00"
  }
]
```

#### 최근 1주일 인기 테마 조회

```http
GET /themes/weeks/top
```

응답 `200 OK`

```json
[
  {
    "id": 1,
    "name": "미드나잇 시그널",
    "thumbnailImageUrl": "https://example.com/theme.jpg",
    "description": "심야 라디오에서 시작되는 미스터리",
    "durationTime": "01:00:00"
  }
]
```

#### 테마 생성

```http
POST /admin/themes
Content-Type: application/json
```

요청

```json
{
  "name": "미드나잇 시그널",
  "thumbnailImageUrl": "https://example.com/theme.jpg",
  "description": "심야 라디오에서 시작되는 미스터리",
  "durationTime": "01:00"
}
```

응답 `201 Created`

```json
{
  "id": 1,
  "name": "미드나잇 시그널",
  "thumbnailImageUrl": "https://example.com/theme.jpg",
  "description": "심야 라디오에서 시작되는 미스터리",
  "durationTime": "01:00:00"
}
```

#### 테마 삭제

```http
DELETE /admin/themes/{id}
```

응답 `204 No Content`

예약에 사용 중인 테마는 삭제할 수 없습니다.

## 주요 에러

검증 실패, 중복, 존재하지 않는 리소스, 사용 중인 리소스 삭제 요청 등은 `4xx` 상태 코드와 메시지 본문으로 응답합니다.
