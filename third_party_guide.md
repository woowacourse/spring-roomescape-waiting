# Toss Payments 연동 가이드 (사전학습 정리)

이 문서는 외부 결제 서비스(Toss Payments) 연동 미션을 시작하기 전, 핵심 개념과 우리 프로젝트
컨벤션에 맞춘 적용 포인트를 정리한 것이다. **결제 도메인 자체가 아니라 외부 API 연동 경험**이
이 미션의 핵심이므로, 딥다이브하지 않고 흐름과 책임 분리만 명확히 짚는다.

## 1. 결제 인증(Authentication) vs 결제 승인(Confirm)

방탈출 예약 서비스에 결제를 끼워 넣으면 플로우는 다음 4단계가 된다.

| 단계                    | 누가          | 무엇을                                              |
|-----------------------|-------------|--------------------------------------------------|
| 1. 렌더링                | 클라이언트       | `renderPaymentWidget`으로 결제 UI 표시                 |
| 2. 요청                 | 클라이언트       | 결제 버튼 클릭 시 `requestPayment` 호출                   |
| 3. 인증(Authentication) | 클라이언트 + 카드사 | 카드 정보 입력/검증, 사기 결제 방지. **서버는 관여하지 않음**           |
| 4. 승인(Confirm)        | **서버**      | `POST /v1/payments/confirm` 호출, 금액·주문 검증 후 실제 출금 |

서버가 직접 코드로 호출하는 것은 **4단계(승인)뿐**이다. 우리가 구현할 부분은 이 한 줄의 API 호출과
그 전후의 검증 로직이다.

```
POST https://api.tosspayments.com/v1/payments/confirm
```

## 2. 클라이언트 vs 서버 역할 분담

| 구분   | 클라이언트(프론트엔드)                                   | 서버(우리가 구현)                                                             |
|------|------------------------------------------------|------------------------------------------------------------------------|
| 키    | `clientKey` (공개 키)                             | `secretKey` (비밀 키, **절대 외부 노출 금지**)                                    |
| 하는 일 | 위젯 렌더링, 결제 요청, `successUrl`/`failUrl` 리다이렉트 처리 | ① 결제 전 `orderId`·최종 `amount` 사전 저장<br>② 리다이렉트로 돌아온 값 검증<br>③ 승인 API 호출 |

`secretKey`는 서버 환경변수/설정으로만 관리하고, 절대 정적 리소스(JS, HTML)나 깃에 커밋하지 않는다.
(`application.properties`에 평문으로 넣지 말고 `application-secret.properties` + `.gitignore` 또는
환경변수 주입을 사용한다.)

## 3. successUrl 리다이렉트 값과 검증 책임

결제 인증이 끝나면 클라이언트는 `successUrl`로 리다이렉트되며 쿼리 파라미터로 다음 값을 받는다.

- `paymentKey` — 토스가 발급하는 거래 고유 식별자. 이후 조회/취소에 필요하므로 **DB에 반드시 저장**.
- `orderId` — 결제 전 서버가 미리 발급/저장해둔 주문 식별자.
- `amount` — 결제 금액.

서버는 이 `orderId`/`amount`를 **사전에 저장해둔 값과 대조**한 뒤 이상이 없을 때만 승인 API를
호출해야 한다. (클라이언트가 보낸 금액을 그대로 믿고 승인하면 금액 조작 공격에 취약해진다.)

검증 순서:

1. 예약 생성 시점에 `orderId`와 예약 금액(`amount`)을 먼저 저장한다.
2. `successUrl`로 돌아온 `orderId`/`amount`를 1번에서 저장한 값과 비교한다.
3. 일치하면 `paymentKey`로 승인 API를 호출한다.
4. 응답 `status`가 `DONE`이면 결제 완료 → 예약을 확정 상태로 전환한다.

## 4. 결제 승인은 한 번만 성공한다

같은 `paymentKey`로 승인 API를 두 번 호출하면 두 번째 호출은 실패하며 토스가 `code`/`message`를
에러 응답으로 반환한다. 즉 승인은 **멱등하지 않은 1회성 작업**이다. 이는 다음 주 2~3단계
(타임아웃, 재시도, Rate Limit)에서 왜 "단순 재시도"가 위험한지와 직결된다 — 타임아웃으로 응답을
못 받았다고 무작정 재요청하면 "이미 승인된 결제"로 에러가 나거나, 반대로 멱등키 없이 재시도 설계를
잘못하면 중복 승인 시도로 의도치 않은 에러 분기를 타게 된다.

## 5. 우리 프로젝트에 적용할 때 참고할 기존 컨벤션

이번 미션의 코드는 기존 패턴을 그대로 따라가면 된다.

- **예외 처리**: `roomescape.common.exception.ErrorCode` enum에 케이스 추가 +
  `BusinessException`으로 던지기. 토스 쪽 에러는 `PAYMENT_CONFIRM_FAILED` 같은 케이스를 추가해서
  토스의 `code`/`message`를 우리 쪽 에러 메시지로 감싸면 된다. 외부 API 응답을 그대로 클라이언트에
  노출하지 않는다.
- **서비스 계층**: `ReservationService`처럼 별도 `PaymentService`를 두고, 외부 호출은 별도
  클라이언트 클래스(`TossPaymentsClient` 등)로 분리해 서비스에서는 그 클라이언트를 주입받아 쓴다.
  (서비스가 HTTP 호출 디테일을 알 필요 없게 한다.)
- **HTTP 클라이언트**: 현재 의존성에는 HTTP 클라이언트가 없다(`build.gradle` 확인 결과 RestTemplate/
  WebClient 모두 미설정). 1단계 요구사항에서 무엇을 쓸지(RestTemplate vs RestClient vs WebClient)
  정해지면 `AppConfig`에 Bean으로 등록하고, 거기서 Connection/Read Timeout을 설정한다(2단계
  요구사항에서 다룰 예정).
- **설정값 분리**: `clientKey`/`secretKey`/승인 API URL은 `application.properties`에
  `toss.client-key`, `toss.secret-key` 형태로 빼고 `@ConfigurationProperties` 또는
  `@Value`로 주입한다. 시크릿 키는 커밋되는 properties 파일에 직접 넣지 않는다.

## 6. 사전학습 조작(2단계) 체크리스트

코드 작성 전에 손으로 먼저 끝까지 해보는 것이 목적이다.

1. [샌드박스 시작하기](https://docs.tosspayments.com/sandbox)에서 테스트 결제 진행 →
   `successUrl` 쿼리 파라미터로 `paymentKey`, `orderId`, `amount` 확보.
2. 결제 승인 API 레퍼런스 페이지의 API 콘솔에서 위 값 + 테스트 시크릿 키로 직접 승인 요청 전송.
3. 응답 `status: DONE` 확인.
4. 같은 `paymentKey`로 한 번 더 승인 요청 → 에러 `code`/`message` 관찰 (1회성 동작 확인).

이 네 가지를 코드 없이 직접 해보고 나서 1단계 요구사항(결제 API 연동 및 예외 핸들링) 구현에
들어가는 것이 이번 주 흐름이다.