# 결제 연동 8단계 — 테스트

## 구현 목표

`TossPaymentGateway` 슬라이스 테스트와 `PaymentService` 단위 테스트를 완성한다.

---

## 추가 테스트 파일

### `TossPaymentGatewayTest.java`

**도구**: `MockRestServiceServer.bindTo(RestClient.Builder)` (Spring 6.1+)  
**방식**: 전체 Spring 컨텍스트 없이 `MockRestServiceServer`만 사용한 경량 슬라이스 테스트

```java
@BeforeEach
void setUp() {
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    mockServer = MockRestServiceServer.bindTo(builder).build();
    gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
}
```

| 테스트 | 검증 내용 |
|---|---|
| `confirm_success_returnsPaymentResult` | 2xx 응답 → `PaymentResult` 반환, 필드 값 일치 |
| `confirm_alreadyProcessed_...` | `ALREADY_PROCESSED_PAYMENT` → `PAYMENT_ALREADY_PROCESSED` |
| `confirm_cardRejected_...` | `CARD_REJECTED` → `PAYMENT_CARD_REJECTED` |
| `confirm_unauthorizedKey_...` | `UNAUTHORIZED_KEY` → `PAYMENT_UNAUTHORIZED_KEY` |
| `confirm_serverError_...` | 5xx + `TOSS_PAYMENTS_ERROR` → `PAYMENT_TOSS_INTERNAL_ERROR` |
| `confirm_unknownErrorCode_...` | 매핑 안 된 code → `PAYMENT_UNKNOWN_ERROR` |

---

### `PaymentServiceTest.java` 보완 (7단계 handlePaymentFail 커버)

| 테스트 | 검증 내용 |
|---|---|
| `handlePaymentFail_withValidOrderId_...` | orderId로 예약 조회 후 CANCELLED 처리 |
| `handlePaymentFail_withNullOrderId_...` | null이면 아무것도 하지 않음 (사용자 직접 취소) |
| `handlePaymentFail_withUnknownOrderId_...` | 없는 orderId → 조용히 무시 |

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| `@RestClientTest` 대신 `MockRestServiceServer.bindTo()` 직접 사용 | `TossPaymentGateway`가 `RestClient.Builder` 대신 `RestClient`를 생성자로 받아 `@RestClientTest` 슬라이스와 연결이 어렵다. `bindTo(builder)` 방식으로 Spring 컨텍스트 없이 동일 효과. |
| ErrorCode 기준으로 어서션 | `CustomException.getMessage()`는 `ErrorCode.getMessage()`이므로 `getErrorCode()` 추출 방식이 더 의미 명확. |
| `PaymentGateway`를 람다 스텁으로 처리 | Mockito 없이 기존 Fake 패턴과 일관성 유지. 단순 성공/실패 시나리오에 충분. |
