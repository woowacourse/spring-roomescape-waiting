# Roomescape Waiting

방탈출 테마, 예약 시간, 확정 예약, 대기 예약을 관리하는 Spring Boot 애플리케이션입니다.

확정 예약이 이미 있는 날짜/시간/테마 조합으로 예약을 요청하면 대기 예약으로 등록됩니다. 확정 예약이 취소되거나 다른 시간대로 변경되면 같은 슬롯의 다음 대기 예약이 자동으로 확정 예약으로 승격됩니다.

## 기술 스택

- Java 21
- Spring Boot 3.4.4
- Spring Web MVC
- Spring JDBC
- Thymeleaf
- H2 Database
- Gradle

## 실행 방법

```bash
./gradlew bootRun
```

애플리케이션은 기본적으로 `http://localhost:8080`에서 실행됩니다.

H2 콘솔은 `http://localhost:8080/h2-console`에서 확인할 수 있습니다.

## 화면

- `GET /`, `GET /reservation`: 사용자 예약 화면
- `GET /admin`: 관리자 화면

## 도메인 규칙

- 테마와 예약 시간은 관리자 API로 생성하고 삭제합니다.
- 삭제된 테마와 예약 시간은 조회되지 않습니다.
- 예약에 사용 중인 테마와 예약 시간은 삭제할 수 없습니다.
- 같은 날짜, 시간, 테마 조합에는 확정 예약이 하나만 존재할 수 있습니다.
- 같은 사용자는 같은 날짜, 시간, 테마 조합에 중복 대기 예약을 만들 수 없습니다.
- 사용자는 본인 이름과 예약 상태를 기준으로 본인의 예약을 취소하거나 변경할 수 있습니다.
- 확정 예약 취소 또는 변경으로 기존 슬롯이 비면 해당 슬롯의 가장 오래된 대기 예약이 확정 예약으로 승격됩니다.

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
    "status": "ACTIVE"
  },
  {
    "id": 2,
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
    "status": "PENDING",
    "pendingOrder": 1
  }
]
```

확정 예약은 `pendingOrder`가 응답에 포함되지 않고, 대기 예약은 현재 대기 순번을 함께 반환합니다.

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

선택한 날짜, 시간, 테마에 이미 확정 예약이 있으면 `PENDING` 상태의 대기 예약으로 생성됩니다.

#### 예약 취소

```http
DELETE /reservations/{id}?username={username}&status={ACTIVE|PENDING}
```

응답 `204 No Content`

확정 예약을 취소하면 같은 슬롯의 다음 대기 예약이 확정 예약으로 승격됩니다. 대기 예약을 취소하면 해당 대기 예약만 취소됩니다.

#### 예약 변경

```http
PATCH /reservations/{id}
Content-Type: application/json
```

요청

```json
{
  "username": "포비",
  "date": "2026-05-28",
  "timeId": 2,
  "themeId": 1,
  "status": "ACTIVE"
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

요청의 `status`는 변경 대상 예약의 현재 상태입니다.

- `ACTIVE` 예약을 빈 슬롯으로 변경하면 확정 예약으로 유지됩니다.
- `ACTIVE` 예약을 이미 확정 예약이 있는 슬롯으로 변경하면 기존 예약은 취소되고 새 슬롯의 대기 예약으로 등록됩니다.
- `PENDING` 예약을 빈 슬롯으로 변경하면 확정 예약으로 승격됩니다.
- `PENDING` 예약을 확정 예약이 있는 슬롯으로 변경하면 대기 예약으로 유지됩니다.

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

확정 예약과 대기 예약을 함께 조회합니다.

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

선택한 날짜에 이미 확정 예약이 있는 시간은 제외됩니다. 현재 시각이 지난 날짜와 시간도 예약 가능 목록에서 제외됩니다.

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

## 에러 응답

에러 본문은 문자열 메시지로 응답합니다.

| 상황 | 상태 코드 |
| --- | --- |
| 요청 값 검증 실패 | `400 Bad Request` |
| 존재하지 않는 리소스 | `404 Not Found` |
| 본인이 아닌 예약 변경 또는 취소 | `401 Unauthorized` |
| 중복 생성 또는 사용 중인 리소스 삭제 | `409 Conflict` |
| 예약할 수 없는 날짜/시간 또는 잘못된 상태 변경 | `422 Unprocessable Entity` |
| 서버 내부 오류 | `500 Internal Server Error` |
