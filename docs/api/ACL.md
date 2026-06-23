# ACL(Anti-Corruption Layer, 부패방지계층)

에릭 에반스의 DDD에서 나온 개념이에요. 핵심 한 줄: **"외부 시스템(다른 모델)의 개념·포맷·에러가 내 도메인 모델로 새어 들어와 오염시키는 걸 막는 번역 경계."**

지금 보고 있는 `TossPaymentGateway`가 교과서적인 ACL 구현이라, 그걸 그대로 해부하면서 설명할게요.

## 왜 "부패 방지"인가

외부 API를 도메인이 직접 알면 어떤 일이 벌어지냐:

- 토스가 `status: "DONE"` 같은 **문자열**을 주는데, 도메인이 이 문자열을 그대로 들고 다니면 → 토스 스펙이 도메인 곳곳에 박힘
- 토스 에러가 `{"code": "REJECT_CARD_PAYMENT"}` 인데, 서비스 코드가 이 문자열을 `if (code.equals("REJECT_CARD_PAYMENT"))`로 분기하면 → 토스를 다른
  PG(나이스페이 등)로 바꾸는 순간 도메인 전체가 깨짐
- 토스가 `RestClient`의 `HttpClientErrorException`을 던지는데 이게 서비스까지 전파되면 → 도메인이 HTTP·Spring을 알게 됨

즉 **외부의 변경이 내부로 전염**되는 게 "부패". ACL은 이 전염을 한 곳에서 끊어요.

## 경계가 어디인가 — 포트 & 어댑터

```
┌─────────────── 도메인 (순수) ───────────────┐
│                                              │
│  PaymentService                              │
│      │  uses                                 │
│      ▼                                        │
│  PaymentGateway (인터페이스 = 포트)           │  ← 도메인 언어로만 말함
│  · confirm(PaymentConfirmation): PaymentResult│
│  · throws TossPaymentException(도메인 예외)   │
└──────────────────┬───────────────────────────┘
                   │ implements
        ╔══════════▼══════════╗
        ║  TossPaymentGateway  ║   ← 여기가 ACL (어댑터)
        ║  ───── 번역 ─────    ║
        ╚══════════┬══════════╝
                   │ HTTP
┌──────────────────▼───────────────────────────┐
│  외부 세계 (토스)                              │
│  · ConfirmRequest / TossPaymentResponse        │
│  · TossErrorResponse {code, message}           │
│  · HTTP status, JSON, RestClient               │
└────────────────────────────────────────────────┘
```

- **포트(`PaymentGateway`)**: 도메인이 "결제를 승인하고 싶다"를 **자기 언어로** 표현. 토스를 전혀 모름. `PaymentConfirmation` 받아서 `PaymentResult`
  돌려주고, 실패하면 `TossPaymentException`(도메인 예외) 던짐.
- **어댑터(`TossPaymentGateway`)**: 그 포트를 토스 HTTP 호출로 **번역**하는 ACL 본체.

## ACL이 하는 3종류의 번역

`confirm()` 한 메서드 안에 세 방향 번역이 다 들어 있어요.

**① 요청 번역 (도메인 → 외부)**

```java
ConfirmRequest confirmRequest = new ConfirmRequest(
        confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
```

도메인 모델 `PaymentConfirmation` → 토스 전용 바디 `ConfirmRequest`. 도메인은 `ConfirmRequest`의 존재조차 몰라요(그래서 이게 `client.dto` 패키지에 격리돼
있죠).

**② 성공 응답 번역 (외부 → 도메인)**

```java
return new PaymentResult(
        response.paymentKey(),
    response.

orderId(),
    PaymentStatus.

from(response.status()),   // "DONE" 문자열 → enum
        response.

totalAmount());                  // totalAmount → approvedAmount 로 이름까지 도메인화
```

토스 `TossPaymentResponse`(필드 9개, 문자열 status) → 도메인 `PaymentResult`(필드 4개, enum). 토스가 안 주는 필드를 추가하든 이름을 바꾸든, 번역 규칙만 여기서
고치면 도메인은 무영향.

**③ 에러 번역 (외부 → 도메인)** ← 아까 얘기한 `of()`

```java
.onStatus(HttpStatusCode::isError, (req, res) ->{
TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
  throw TossPaymentException.

of(res.getStatusCode(),error);  // 토스 코드 → 도메인 예외
        });
```

HTTP 4xx/5xx + `{"code": "..."}` → 의미 있는 도메인 예외(`CardRejected`, `Retryable`...). 서비스는 "카드가 거절됐다"는 **도메인 사실**만 알면 되지, "
토스가 403에 REJECT_CARD_PAYMENT를 줬다"는 외부 사정은 몰라도 됨.

## 이 구조가 주는 것

| 효과         | 구체적으로                                                                                                     |
|------------|-----------------------------------------------------------------------------------------------------------|
| **교체 가능성** | PG를 나이스페이로 바꿔도 `NicePayGateway implements PaymentGateway` 새 어댑터만 추가. 도메인·서비스 코드 0줄 수정.                    |
| **테스트 격리** | `PaymentServiceTest`는 `PaymentGateway`를 mock으로 갈아끼움 → HTTP 없이 도메인 로직만 검증. ACL 자체는 `MockWebServer`로 따로 검증. |
| **에러 의미화** | `Retryable`은 재시도, `GatewayConfig`는 운영 알람 — HTTP 500/401이 아니라 **대응 전략별 예외**로 승격 (주석에 그 의도가 적혀 있죠).         |
| **변경 국소화** | 토스 스펙 변경 → `TossPaymentGateway` + DTO만 수정. 파급 차단.                                                         |

## "어댑터(Adapter) 패턴이랑 뭐가 달라?"

자주 헷갈리는 포인트라 짚으면:

- **Adapter 패턴**(GoF): 인터페이스 모양만 맞춰주는 기계적 변환. 보통 1:1, 의미 변환 없음.
- **ACL**(DDD): Adapter를 **포함**하지만 더 큼. 모델 자체를 번역하고(`status` 문자열→enum), 에러 의미를 재정의하고(`Retryable`), 외부 개념이 안 새도록 **방어**하는
  게 목적. ACL 내부 구현 수단으로 Adapter·Facade·Translator를 씁니다.

즉 ACL ⊃ Adapter. `TossPaymentGateway`는 "토스 어댑터"이자 "결제 ACL"인 거예요.

## 핵심 규칙 하나로 압축

> **토스의 타입(`ConfirmRequest`, `TossPaymentResponse`, `TossErrorResponse`)과 Spring HTTP 타입은 `client` 패키지 밖으로 절대 나가지 않는다.
경계를 넘는 건 오직 도메인 타입(`PaymentConfirmation`, `PaymentResult`, `TossPaymentException`)뿐이다.**

이 규칙이 지켜지는지(=`import woowacourse.payment.client.dto.*`가 서비스/도메인 패키지에 등장하지 않는지)가 ACL이 제대로 섰는지 판별하는 리트머스지입니다. 클래스 맨 위
Javadoc — "Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층)" — 가 정확히 이 얘기예요.