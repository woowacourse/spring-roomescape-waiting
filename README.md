## 사이클1 - 기능목록

### 테마 도메인

* [x] 테마 도메인 추가
    * [x] 테마는 이름, 설명, 썸네일 이미지 url을 가진다
    * 모든 테마의 시작 시간과 소요 시간은 동일하다고 가정한다
* [x] 예약에 테마 정보가 포함 되도록 수정
* [x] 관리자의 테마 추가, 삭제

### 사용자 예약

* [x] 사용자의 날짜, 테마 선택 -> 예약 가능한 시간 표시
    * [x] 예약 가능한 시간이란 관리자가 등록한 시간 중 해당 날짜+테마에 아직 예약이 없는 시간이다
* [x] 사용자가 본인의 이름으로 예약 가능한 시간으로 예약
* [x] 같은 날짜, 시간이라도 테마가 다르면 각각 예약 가능

### 인기 테마 조회

* [x] 최근 1주일동안 예약이 많았던 테마 상위 10개 조회
    * [x] 최근 1주일을 이전 날 기준으로 조회한다

### 추가 요구사항

* [x] `data.sql`로 테스트용 데이터 넣어서 인기 테마 조회 결과 검증

### 화면

* [x] 사용자가 보는 화면 출력
    * [x] 브라우저에서 정상 동작 확인까지만 한다

## 사이클2 - 1단계 서비스 정책

* [x] 지난 날짜·시간 예약 거부
* [x] 같은 날짜·시간·테마 중복 예약 거부
* [x] 예약이 존재하는 시간 삭제 거부
* [x] 유효하지 않은 입력값 거부 (빈 이름, 잘못된 날짜/시간 형식)

## 사이클2 - 2단계 에러 응답 설계

* [x] 전역 예외 처리 도입 (`@RestControllerAdvice` / `@ExceptionHandler`)
* [x] 공통 에러 응답 본문 형식 (RFC 9457 Problem Details, `application/problem+json`)
* [x] 예외 계층 도입 (`NotFoundException` 404 / `ConflictException` 409 / `BusinessRuleViolationException` 422)
* [x] 처리되지 않은 예외가 500으로 새지 않도록 보강
* [x] 클라이언트 화면 에러 메시지 노출

## 사이클2 - 3단계 내 예약 조회/변경/취소

* [x] 본인 예약 목록 조회 (이름 기준)
* [x] 본인 예약 취소
* [x] 본인 예약 날짜·시간 변경
* [x] 변경·취소 엣지 케이스 처리 (지난 예약 거부, 시간 충돌 거부 등) — 2단계 ProblemDetail 규칙대로
* [x] README에 API 명세 및 에러 응답 명세 정리

## API 명세

성공 응답은 `application/json`, 에러 응답은 `application/problem+json`.

### 관리자 API

| 메서드 | 경로 | 요청 | 성공 응답 |
|---|---|---|---|
| GET | `/reservations` | `?page=0&size=20` (선택) | 200 `{ reservations: [...] }` |
| POST | `/reservations` | `{ name, date, timeId, themeId }` | 201 `{ id, name, date, time, theme }` |
| DELETE | `/reservations/{id}` | | 204 |
| GET | `/times` | | 200 `{ times: [...] }` |
| POST | `/times` | `{ startAt }` | 201 `{ id, startAt }` |
| DELETE | `/times/{id}` | | 204 |
| GET | `/themes` | | 200 `{ themes: [...] }` |
| POST | `/themes` | `{ name, description, thumbnailImageUrl }` | 201 `{ id, name, description, thumbnailImageUrl }` |
| DELETE | `/themes/{id}` | | 204 |
| GET | `/themes/popular` | `?now=YYYY-MM-DD&days=7&limit=10` (모두 선택) | 200 `{ themes: [...] }` |

### 사용자 API

| 메서드 | 경로 | 요청 | 성공 응답 |
|---|---|---|---|
| GET | `/times/availability` | `?date=YYYY-MM-DD&themeId={id}` | 200 `{ times: [{ id, startAt, reserved }] }` |
| GET | `/reservations/me` | `?name={이름}` | 200 `{ reservations: [...] }` |
| PUT | `/reservations/me/{id}` | `?name={이름}` + `{ date, timeId }` | 200 `{ id, name, date, time, theme }` |
| DELETE | `/reservations/me/{id}` | `?name={이름}` | 204 |

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
    { "pointer": "/name", "reason": "이름은 비어 있을 수 없습니다." }
  ]
}
```

| 상태 | type slug | 발생 조건 |
|---|---|---|
| 400 | `validation-error` | 요청 본문이 `@Valid` 검증 실패 |
| 400 | `bad-request` | 필수 쿼리 파라미터 누락, 요청 본문 파싱 실패, 경로 변수 타입 불일치 등 일반 잘못된 요청 |
| 401 | `unauthorized` | 다른 사람 이름으로 본인 예약 변경·취소 시도 |
| 404 | `not-found` | 도메인 리소스 미존재 (예: 예약 id) |
| 404 | `no-resource` | 정적 리소스 미존재 (Spring MVC `NoResourceFoundException`) |
| 405 | `method-not-supported` | 지원하지 않는 HTTP 메서드 |
| 406 | `not-acceptable` | 응답 가능한 미디어 타입 없음 |
| 409 | `conflict` | 동일 날짜·시간·테마 중복 예약 |
| 415 | `media-type-not-supported` | 지원하지 않는 요청 미디어 타입 |
| 422 | `business-rule-violation` | 지난 시각 예약/변경, 예약이 존재하는 시간·테마 삭제 |
| 500 | `internal-error` | 처리되지 않은 예외 |
