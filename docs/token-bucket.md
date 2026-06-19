# Token Bucket Rate Limiter

## 개요
외부 의존성 없이 처리율 제한(Rate Limiting)을 위해 구현된 Token Bucket 알고리즘입니다. 단기간의 폭발적인 트래픽(Burst)을 허용하면서도, 장기적으로는 평균 처리율을 유지합니다.

## 주요 개념
- **Capacity (용량):** 버킷에 담길 수 있는 최대 토큰 수입니다. 허용 가능한 최대 버스트 크기를 결정합니다.
- **Refill Rate (보충률):** 초당 보충되는 토큰의 개수(`refillPerSec`)입니다. 평균 TPS 상한을 결정합니다.
- **Tokens (현재 토큰):** 현재 소비 가능한 토큰의 양입니다. 실수(double) 타입으로 관리하여 정밀한 보충을 지원합니다.

## 구현 세부사항

### 1. 토큰 보충 (Lazy Refill)
별도의 스케줄러 스레드 없이, `tryConsume()` 또는 `retryAfterSeconds()`가 호출될 때마다 마지막 호출 이후 경과 시간을 계산하여 토큰을 보충합니다.
- 보충량 = `(현재 시간 - 마지막 보충 시간) × refillPerSec`
- 현재 토큰 = `min(capacity, 이전 토큰 + 보충량)`

### 2. 처리율 제한 로직
- **`tryConsume()`:** 
  - 토큰이 1개 이상 존재하면 1개를 차감하고 `true`를 반환합니다.
  - 토큰이 부족하면 `false`를 반환합니다.
- **`retryAfterSeconds()`:**
  - 토큰이 부족할 경우, 1개가 찰 때까지 필요한 최소 시간을 초 단위로 계산하여 반환합니다.
  - `Math.ceil()`을 사용하여 소수점 이하는 올림 처리합니다.

### 3. 동시성 제어
`synchronized` 키워드를 사용하여 여러 스레드에서 동시에 토큰을 소비하거나 보충할 때 데이터 정합성을 보장합니다. 토큰의 상태 업데이트와 소비가 원자적으로 이루어집니다.

### 4. 테스트 용이성 (Testability)
`System.nanoTime()`에 직접 의존하지 않고, 생성 시 `LongSupplier`를 주입받아 시간을 결정적으로 제어할 수 있도록 설계되었습니다. 이를 통해 테스트 코드에서 가짜 시계(Fake Clock)를 사용하여 시간 흐름을 시뮬레이션할 수 있습니다.

## 서버(게이트웨이) 적용

### 1. HandlerInterceptor를 통한 요청 제어
`RateLimitInterceptor`는 모든 결제 및 예약 관련 엔드포인트에 적용되어 요청을 사전에 필터링합니다.
- **적용 경로:** `/reservations/**`, `/payments/**`
- **동작 방식:** `preHandle` 단계에서 토큰 소비를 시도하고, 실패 시 `429 Too Many Requests` 상태 코드와 함께 `Retry-After` 헤더를 설정하여 즉시 응답을 반환합니다.

### 2. 설정의 외부화
Spring의 `@ConfigurationProperties`를 사용하여 코드 수정 없이 처리율 제한 설정을 변경할 수 있습니다.
- `rate-limit.capacity`: 최대 버스트 허용량
- `rate-limit.refill-per-sec`: 초당 평균 처리 상한

### 3. 응답 헤더 (Retry-After)
한도 초과 시 반환되는 `Retry-After` 헤더는 클라이언트가 다음 요청을 시도하기까지 대기해야 하는 최소 시간(초)을 나타냅니다. 이는 `TokenBucket.retryAfterSeconds()`에서 계산된 값을 사용하며, 항상 올림된 정수 값을 가집니다.

## 사용 예시
```java
// WebConfig에서 인터셉터 등록
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/reservations/**", "/payments/**");
}
```

```properties
# application.properties 예시
rate-limit.capacity=10.0
rate-limit.refill-per-sec=2.0
```
