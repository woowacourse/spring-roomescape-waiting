# 결제 연동 5단계 — TossPaymentGateway 어댑터

## 구현 목표

`PaymentGateway` 포트의 Toss 구현체를 만든다.  
서비스 계층은 Toss를 모르고, Toss DTO는 이 어댑터 밖으로 절대 유출되지 않는다.

---

## 추가 파일

### `roomescape/infra/toss/dto/TossPaymentRequest.java`
```java
record TossPaymentRequest(String paymentKey, String orderId, Long amount)
```
Toss 승인 API 요청 body.

### `roomescape/infra/toss/dto/TossPaymentResponse.java`
```java
record TossPaymentResponse(String paymentKey, String orderId, Long totalAmount)
```
Toss 승인 API 성공 응답. `totalAmount` 필드명은 Toss 스펙 그대로.

### `roomescape/infra/toss/dto/TossErrorResponse.java`
```java
record TossErrorResponse(String code, String message)
```
Toss 에러 응답 역직렬화용.

### `roomescape/global/config/TossPaymentConfig.java`
```java
@Bean
public RestClient tossRestClient() {
    String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
    return RestClient.builder()
            .baseUrl("https://api.tosspayments.com")
            .defaultHeader("Authorization", "Basic " + encoded)
            .defaultHeader("Content-Type", "application/json")
            .build();
}
```
- secretKey는 `@Value("${toss.secret-key}")` 주입
- Authorization: `Basic base64(secretKey + ":")`

### `roomescape/infra/toss/TossPaymentGateway.java`

핵심 흐름:
```
POST /v1/payments/confirm
  → 성공(2xx): TossPaymentResponse → PaymentResult 변환 반환
  → 에러(4xx/5xx): TossErrorResponse 역직렬화 → code 기반으로 ErrorCode 매핑 → CustomException
```

에러 코드 매핑:

| Toss code | ErrorCode |
|---|---|
| `ALREADY_PROCESSED_PAYMENT` | `PAYMENT_ALREADY_PROCESSED` |
| `CARD_REJECTED`, `EXCEED_MAX_*` | `PAYMENT_CARD_REJECTED` |
| `UNAUTHORIZED_KEY`, `INVALID_API_SECRET_KEY` | `PAYMENT_UNAUTHORIZED_KEY` |
| `NOT_FOUND_PAYMENT`, `NOT_FOUND_PAYMENT_SESSION` | `PAYMENT_NOT_FOUND` |
| `TOSS_PAYMENTS_ERROR` | `PAYMENT_TOSS_INTERNAL_ERROR` |
| 그 외 | `PAYMENT_UNKNOWN_ERROR` |

---

## 변경 파일

### `roomescape/global/exception/ErrorCode.java`

결제 관련 에러 코드 7종 추가:

| ErrorCode | HttpStatus | message |
|---|---|---|
| `PAYMENT_AMOUNT_MISMATCH` | 422 | 결제 금액이 주문 금액과 일치하지 않습니다. |
| `PAYMENT_ALREADY_PROCESSED` | 400 | 이미 처리된 결제입니다. |
| `PAYMENT_CARD_REJECTED` | 403 | 카드 결제가 거절되었습니다. |
| `PAYMENT_UNAUTHORIZED_KEY` | 401 | 결제 인증 키가 유효하지 않습니다. |
| `PAYMENT_NOT_FOUND` | 404 | 결제 정보를 찾을 수 없습니다. |
| `PAYMENT_TOSS_INTERNAL_ERROR` | 502 | 결제 서버 내부 오류입니다. 잠시 후 재시도해 주세요. |
| `PAYMENT_UNKNOWN_ERROR` | 500 | 결제 처리 중 알 수 없는 오류가 발생했습니다. |

### `src/main/resources/application.properties`

```properties
toss.secret-key=test_sk_placeholder
```

실제 키는 환경 변수 또는 별도 설정 파일로 분리 필요.

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| Toss DTO를 `infra/toss/dto/` 하위에만 격리 | ACL 패턴. 어댑터 안에서만 Toss 명세를 알고, 서비스/도메인 계층에는 `PaymentConfirmation`/`PaymentResult`만 노출 |
| `onStatus` 안에서 `ObjectMapper`로 직접 역직렬화 | `ClientHttpResponse.getBody()`는 `InputStream` 반환. Spring RestClient의 `ResponseSpec.ErrorHandler` 시그니처 상 `bodyTo()`가 없어 직접 파싱 필요 |
| `objectMapper` 생성자 주입 | Spring Boot 자동 구성 `ObjectMapper` 빈을 재사용. 직렬화 설정 일관성 유지 |
| response null 가드 | `body(Class)` 반환값이 null일 수 있는 엣지케이스 방어 |
