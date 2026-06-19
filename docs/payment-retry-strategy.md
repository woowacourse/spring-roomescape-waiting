# 결제 재시도 전략 (Payment Retry Strategy)

## 개요
외부 결제 게이트웨이(Toss Payments) 호출 시 발생할 수 있는 처리율 제한(429 Too Many Requests) 상황을 우아하게 처리하기 위해 백오프(Backoff) 재시도 로직을 도입했습니다.

## 구현 세부사항

### 1. TossRetryInterceptor (ClientHttpRequestInterceptor)
`RestClient` 요청 과정에서 응답을 가로채어 재시도를 수행합니다.
- **대상 응답:** HTTP 429 Too Many Requests
- **대기 방식:** 
  1. 응답 헤더의 `Retry-After` 값이 있으면 해당 시간(초)만큼 대기합니다.
  2. `Retry-After` 헤더가 없으면 기본값(1초)만큼 대기합니다.
- **최대 시도 횟수:** `payment.toss.max-attempts` 설정을 따르며, 이를 초과할 경우 최종적으로 `TossPaymentException`을 던져 상위 레이어에서 에러로 처리하게 합니다.

### 2. 멱등성 유지 (Idempotency)
재시도 시에도 최초 요청과 동일한 **주문당 고정 멱등키(Idempotency-Key)**를 유지합니다.
- 이는 네트워크 타임아웃 등으로 인해 결제 승인 여부가 불분명한 상태(Unknown State)에서 중복 승인이 발생하는 것을 원천적으로 방지합니다.
- 429 응답은 토스 측에서 아직 요청을 처리하지 않았음을 의미하므로, 동일한 멱등키로 다시 요청을 보내는 것이 안전합니다.

### 3. 무한 재시도 방지
무분별한 재시도로 인한 리소스 고갈을 막기 위해 시도 횟수를 엄격히 제한합니다.
- 설정된 `max-attempts` 이후에도 실패하면 즉시 실패 처리하여 사용자에게 알립니다.

## 설정 방법 (application.properties)
```properties
# 최대 재시도 횟수 (기본값: 3)
payment.toss.max-attempts=3
```

## 검증 내역
- `TossRetryInterceptorTest`: 429 응답 시 `Retry-After` 헤더를 준수하여 재시도하는지, 최대 횟수 초과 시 적절히 실패하는지 검증 완료.
- `TossClientTimeoutTest`: 기존 타임아웃 테스트와의 호환성 확인 완료.
