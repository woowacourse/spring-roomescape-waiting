# 🗒️ 용어 정의 및 모델링

### 슬롯

| 용어    | 설명                                |
|-------|-----------------------------------|
| slot  | date + time + theme로 구성된 예약 가능 단위 |
| date  | 예약 날짜                             |
| time  | 예약 시각                             |
| theme | 방탈출 테마                            |

### 예약 시간

| 용어               | 설명        |
|------------------|-----------|
| reservation time | 예약 시간     |
| start at         | 슬롯의 시작 시간 |

### 테마

| 용어                  | 설명      |
|---------------------|---------|
| theme               | 방탈출 테마  |
| theme name          | 테마 이름   |
| theme description   | 테마 설명   |
| theme thumbnail url | 테마 썸네일  |
| theme runtime       | 테마 소요시간 |

### 예약

| 용어          | 설명                            |
|-------------|-------------------------------|
| reservation | 방탈출 예약 정보                     |
| reserver    | 예약자                           |
| slot        | 예약하는 슬롯 (date + time + theme) |

### 대기

| 용어      | 설명                            |
|---------|-------------------------------|
| waiting | 다른 사용자에 의해 예약된 슬롯에 신청하는 것     |
| waiter  | 대기자                           |
| order   | 대기 순번                         |
| slot    | 대기하는 슬롯 (date + time + theme) |

### 사용자

| 용어     | 설명                              |
|--------|---------------------------------|
| member | 사용자 / 예약의 reserver / 대기의 waiter |
| name   | 사용자 이름                          |

# 🗒️ 기능 목록

## 1단계 - 결제 API 연동 및 예외 핸들링

- [ ] 결제 전 주문 정보 저장
    - [ ] 예약 생성 요청이 들어오면 결제 인증 전에 주문 정보(orderId, 최종 amount)를 먼저 저장한다.
    - [ ] orderId는 서버가 생성하며(6~64자, 영숫자/-/_) 이후 금액 검증의 기준이 된다.
    - [ ] 이 시점의 예약은 아직 확정이 아니다(결제 대기).

- [ ] 브라우저 결제창 연동 (클라이언트)
    - [ ] 예약 페이지에 Toss 결제창 SDK를 붙여 인증받는다(위젯 초기화는 클라이언트 키 test_ck_).
    - [ ] 카드 정보 입력·인증은 결제창과 카드사가 처리하며 서버는 카드번호를 절대 만지지 않는다.
    - [ ] 인증이 성공하면 토스가 successUrl로 paymentKey, orderId, amount를 넘긴다.

- [ ] successUrl 콜백 — 금액 검증 후 승인
    - [ ] 콜백으로 넘어온 amount를 그대로 믿지 않고 주문 저장 금액과 대조한다.
    - [ ] 다르면 PaymentAmountMismatch류 예외로 승인 호출 전에 차단한다.
    - [ ] 토스엔 금액 불일치 전용 코드가 없어 서버가 직접 검증해야 한다.
    - [ ] 일치하면 승인 API를 호출한다. 성공하면 이후 조회·취소에 필요한 paymentKey를 DB에 저장하고 예약을 CONFIRMED로 바꾼다.

- [ ] 결제 승인 API 호출 (RestClient)
    - [ ] POST https://api.tosspayments.com/v1/payments/confirm를 RestClient로 호출한다.
        - [ ] 바디 3필드: paymentKey, orderId, amount (Content-Type application/json).
        - [ ] 인증은 Basic: base64(시크릿키 + ":")를 Authorization: Basic ...로 보낸다(콜론 뒤 비밀번호는 비우고, 인코딩 시 UTF-8 명시).
        - [ ] 시크릿 키(test_sk_)는 노출/하드코딩 금지 — application.yaml 등으로 외부화한다. 시크릿 키는 서버 승인 전용이다(클라이언트 키와 역할이 다름).

- [ ] 관심사 분리 — 포트 & 어댑터
    - [ ] 도메인/애플리케이션 계층에 PaymentGateway 포트와 도메인 모델(PaymentConfirmation, PaymentResult)을 둔다.
    - [ ] PaymentService는 Toss와 Toss DTO를 몰라야한다.
    - [ ] Toss DTO(요청/응답/에러) ↔ 도메인 모델 번역은 어댑터 TossPaymentGateway(부패 방지 계층, ACL)가 맡는다. PG사를 바꿔도 어댑터만 새로 만들면 되고 도메인은 그대로다.

- [ ] 에러 응답을 도메인 예외로 매핑
    - [ ] onStatus(HttpStatusCode::isError, 핸들러)로 4xx/5xx를 가로챈다.
    - [ ] 핸들러에서 본문을 TossErrorResponse({code, message})로 역직렬화한 뒤 도메인 예외로 변환한다.
    - [ ] 변환은 어댑터 안에서 일어나고 Toss DTO는 밖으로 새지 않는다.
    - [ ] 변환한 예외는 사용자 응답으로도 의미 있게 이어진다(카드 거절은 안내, 키 오류는 알람 등).
    - [ ] code별 분기 방향(자기 서비스에 맞게 설계)

| HTTP | code                                                              | 처리 방향             |
|------|-------------------------------------------------------------------|-------------------|
| 400  | ALREADY_PROCESSED_PAYMENT                                         | 이미 승인됨(재시도·새로고침)  |
| 400  | DUPLICATED_ORDER_ID / NOT_FOUND_PAYMENT_SESSION / INVALID_REQUEST | 중복·만료·잘못된 요청      |
| 401  | UNAUTHORIZED_KEY / INVALID_API_KEY                                | 키 설정 오류 — 운영 알람   |
| 403  | REJECT_CARD_PAYMENT                                               | 카드 거절 — 사용자 안내    |
| 404  | NOT_FOUND_PAYMENT                                                 | 결제 건 없음           |
| 500  | FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING                         | 토스 내부 오류 — 재시도 대상 |
| 그 외  | 미정의                                                               | 기본 예외             |

- [ ] failUrl(취소/실패) 처리
    - [ ] failUrl로 code, message, orderId가 넘어온다.
    - [ ] 실패 사유를 사용자에게 보여주고 결제 대기 상태의 주문/예약을 정리한다.
    - [ ] 단, 사용자가 취소(PAY_PROCESS_CANCELED)하면 orderId가 없을 수 있으니 null 가드를 둔다.

# 완료 기준

- [ ] 테스트 카드로 결제 인증 → 승인 → 예약 확정(CONFIRMED) 전체 흐름이 동작한다.
- [ ] 조작된 amount는 승인 호출 전에 차단되고 게이트웨이가 호출되지 않는다.
- [ ] 주요 에러코드(이미 처리됨/카드 거절/키 오류/재시도 대상)가 도메인 예외와 사용자 응답으로 처리되고, 미정의 코드는 기본 예외로 떨어진다.
- [ ] PaymentService가 Toss를 모른다(Toss DTO·에러 매핑이 어댑터 뒤로 격리됨).
- [ ] 시크릿 키가 설정으로 외부화되어 있고, failUrl의 orderId 없는 취소에서도 NPE가 없다.

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

| 메서드    | 경로                      | 요청                                | 성공 응답                                                      |
|--------|-------------------------|-----------------------------------|------------------------------------------------------------|
| GET    | `/times/availability`   | `?date=YYYY-MM-DD&themeId={id}`   | 200 `{ times: [{ id, startAt, reserved }] }`               |
| POST   | `/reservations`         | `{ name, date, timeId, themeId }` | 201 `{ id, name, date, time, theme }`                      |
| GET    | `/reservations/me`      | `?name={이름}`                      | 200 `{ reservations: [...] }`                              |
| PUT    | `/reservations/me/{id}` | `?name={이름}` + `{ date, timeId }` | 200 `{ id, name, date, time, theme }`                      |
| DELETE | `/reservations/me/{id}` | `?name={이름}`                      | 204                                                        |
| POST   | `/waitings`             | `{ name, date, time, theme }`     | 201 `{ id, name, date, time, theme, order }`               |
| GET    | `/waitings/me`          | `?name={이름}`                      | 200 `{ waitings: [ id, name, order, date, time, theme ] }` |
| DELETE | `/waitings/me/{id}`     | `?name={이름}`                      | 204                                                        |

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

## Development Conventions

- [Time Handling Convention](docs/conventions/time-handling.md)
