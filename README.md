# 🗒️ 용어 정의 및 모델링

### 예약

| 용어          | 설명                                |
|-------------|-----------------------------------|
| reservation | 방탈출 예약 정보. 예약자 이름과 방문 날짜, 시각을 포함. |
| name        | 예약자 이름. (10자 이내)                  |
| date        | 예약 날짜.                            |
| time        | 예약 시각. 시간은 슬롯으로 관리된다.             |
| theme       | 방탈출 테마. 시작시간과 소요시간이 동일하다.         |

### 예약 시간

| 용어               | 설명                       |
|------------------|--------------------------|
| reservation time | 예약 시간                    |
| start at         | 시간을 슬롯으로 관리할 때 슬롯의 시작 시간 |

### 테마

| 용어                  | 설명      |
|---------------------|---------|
| theme               | 방탈출 테마. |
| theme name          | 테마 이름   |
| theme description   | 테마 설명   |
| theme thumbnail url | 테마 썸네일  |
| theme runtime       | 테마 소요시간 |

### 슬롯

| 용어   | 설명                          |
|------|-----------------------------|
| slot | date + time + theme로 구성된 단위 |

### 대기

| 용어      | 설명                        |
|---------|---------------------------|
| waiting | 다른 사용자에 의해 예약된 슬롯에 신청하는 것 |
| order   | 대기 순번                     |

# 🗒️ 기능 목록

## 1단계 - 예약 대기 신청/취소

- [ ] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
- [ ] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
- [ ] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
- [ ] 사용자는 본인의 대기를 취소할 수 있다.

## 2단계 - 내 예약 목록 조회 (상태 구분)

- [ ] 이전 미션의 내 예약 목록 조회를 확장한다.
- [ ] 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
- [ ] 대기에는 본인의 대기 순번도 함께 보여준다.

## API 명세

성공 응답은 `application/json`, 에러 응답은 `application/problem+json`.

### 관리자 API

| 메서드    | 경로                   | 요청                                         | 성공 응답                                              |
|--------|----------------------|--------------------------------------------|----------------------------------------------------|
| GET    | `/reservations`      | `?page=0&size=20` (선택)                     | 200 `{ reservations: [...] }`                      |
| DELETE | `/reservations/{id}` |                                            | 204                                                |
| GET    | `/times`             |                                            | 200 `{ times: [...] }`                             |
| POST   | `/times`             | `{ startAt }`                              | 201 `{ id, startAt }`                              |
| DELETE | `/times/{id}`        |                                            | 204                                                |
| GET    | `/themes`            |                                            | 200 `{ themes: [...] }`                            |
| POST   | `/themes`            | `{ name, description, thumbnailImageUrl }` | 201 `{ id, name, description, thumbnailImageUrl }` |
| DELETE | `/themes/{id}`       |                                            | 204                                                |
| GET    | `/themes/popular`    | `?now=YYYY-MM-DD&days=7&limit=10` (모두 선택)  | 200 `{ themes: [...] }`                            |

### 사용자 API

| 메서드    | 경로                      | 요청                                | 성공 응답                                        |
|--------|-------------------------|-----------------------------------|----------------------------------------------|
| GET    | `/times/availability`   | `?date=YYYY-MM-DD&themeId={id}`   | 200 `{ times: [{ id, startAt, reserved }] }` |
| POST   | `/reservations`         | `{ name, date, timeId, themeId }` | 201 `{ id, name, date, time, theme }`        |
| GET    | `/reservations/me`      | `?name={이름}`                      | 200 `{ reservations: [...] }`                |
| PUT    | `/reservations/me/{id}` | `?name={이름}` + `{ date, timeId }` | 200 `{ id, name, date, time, theme }`        |
| DELETE | `/reservations/me/{id}` | `?name={이름}`                      | 204                                          |
| POST   | `/waitings`             | `{ name, date, timeId, themeId }` | 201 `{ id, name, date, time, theme, order }` |

## 에러 응답

RFC 9457 Problem Details 형식. `Content-Type: application/problem+json`.

```json
{
  "type": "https://roomescape.example/problems/business-rule-violation",
  "title": "비즈니스 정책 위반",
  "status": 422,
  "detail": "지난 시각으로 예약을 변경할 수 없습니다.",
  "instance": "/reservations/me/3"
}
```

요청 본문 검증 실패(`400 validation-error`)는 `errors` 배열이 추가된다.

```json
{
  "type": "https://roomescape.example/problems/validation-error",
  "title": "요청 본문 검증 실패",
  "status": 400,
  "detail": "요청 본문의 일부 필드가 유효하지 않습니다.",
  "instance": "/reservations",
  "errors": [
    {
      "pointer": "/name",
      "reason": "이름은 비어 있을 수 없습니다."
    }
  ]
}
```

| 상태  | type slug                  | 발생 조건                                                |
|-----|----------------------------|------------------------------------------------------|
| 400 | `validation-error`         | 요청 본문이 `@Valid` 검증 실패                                |
| 400 | `bad-request`              | 필수 쿼리 파라미터 누락, 요청 본문 파싱 실패, 경로 변수 타입 불일치 등 일반 잘못된 요청 |
| 401 | `unauthorized`             | 다른 사람 이름으로 본인 예약 변경·취소 시도                            |
| 404 | `not-found`                | 도메인 리소스 미존재 (예: 예약 id)                               |
| 404 | `no-resource`              | 정적 리소스 미존재 (Spring MVC `NoResourceFoundException`)   |
| 405 | `method-not-supported`     | 지원하지 않는 HTTP 메서드                                     |
| 406 | `not-acceptable`           | 응답 가능한 미디어 타입 없음                                     |
| 409 | `conflict`                 | 동일 날짜·시간·테마 중복 예약                                    |
| 415 | `media-type-not-supported` | 지원하지 않는 요청 미디어 타입                                    |
| 422 | `business-rule-violation`  | 지난 시각 예약/변경, 예약이 존재하는 시간·테마 삭제                       |
| 500 | `internal-error`           | 처리되지 않은 예외                                           |
