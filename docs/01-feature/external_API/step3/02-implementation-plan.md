# Step3 구현 계획 — Rate Limit (토큰 버킷)

---

## 결론 먼저

> 변경이 필요한 레이어는 4곳이다.
> `TokenBucketRateLimiter` (핵심 알고리즘) →
> `RateLimitInterceptor` (HandlerInterceptor — 들어오는 요청 제한) →
> `RetryAfterInterceptor` (ClientHttpRequestInterceptor — 토스 429 재시도) →
> `OutboundRateLimitInterceptor` (ClientHttpRequestInterceptor — 나가는 호출 제한)
>
> 알고리즘은 토큰 버킷 하나, 방향은 들어오는/나가는 둘이다.

---

## 현재 코드 상태 파악

### 무엇이 없는가

| 위치 | 없는 것 |
|------|---------|
| (신규) `TokenBucketRateLimiter` | 토큰 버킷 핵심 알고리즘 |
| (신규) `RateLimitInterceptor` | 들어오는 요청 429 거부 HandlerInterceptor |
| (신규) `RetryAfterInterceptor` | 토스 429 → Retry-After 백오프 재시도 |
| (신규) `OutboundRateLimitInterceptor` | 나가는 호출 자체 Rate Limit |
| `TossPaymentConfig` | RestClient에 인터셉터 등록 |
| `WebMvcConfig` (또는 기존 설정 클래스) | HandlerInterceptor 경로 등록 |
| `application.yml` | `rate-limit.*`, `outbound-rate-limit.*` 설정 외부화 |
| `ErrorCode` | `RATE_LIMIT_EXCEEDED`, `OUTBOUND_RATE_LIMIT_EXCEEDED`, `TOSS_RATE_LIMIT_EXCEEDED` |

### 무엇이 이미 있는가

- `TossPaymentConfig` → `RestClient` 빈 설정, `SimpleClientHttpRequestFactory` 타임아웃 설정 완료.
- step2에서 도입한 `@Retryable` (read timeout 재시도)는 토큰 버킷 재시도와 레이어가 다르므로 충돌하지 않는다.
- `GlobalExceptionHandler` → `CustomException` 핸들러 완성. `RATE_LIMIT_EXCEEDED`는 자동으로 처리된다.

---

## 구현 순서

### Step 1 — TokenBucketRateLimiter 구현

요구사항의 핵심. 외부 의존성 없이 직접 구현한다.

#### 설계

```java
public class TokenBucketRateLimiter {

    private final long capacity;        // 최대 보유 가능 토큰 (버스트 허용량)
    private final double refillPerSec;  // 초당 보충 토큰 수 (평균 TPS 상한)
    private final LongSupplier clock;   // 가짜 시계 주입용 (System::nanoTime)

    private double tokens;
    private long lastRefillNanos;

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    public synchronized long retryAfterSeconds() {
        refill();
        // 1개가 찰 때까지 걸리는 시간 (초, 올림)
        double secondsNeeded = (1.0 - tokens) / refillPerSec;
        return (long) Math.ceil(secondsNeeded);
    }

    private void refill() {
        long now = clock.getAsLong();
        double elapsed = (now - lastRefillNanos) / 1_000_000_000.0;
        tokens = Math.min(capacity, tokens + elapsed * refillPerSec);
        lastRefillNanos = now;
    }
}
```

#### 핵심 결정사항

**왜 `double tokens`인가?**
`long`으로 정수 토큰만 관리하면 보충 주기가 1초 미만일 때 매번 0이 되어
소수점 토큰 손실이 쌓인다. `double`로 분수 토큰을 유지해야 정확하다.

**왜 `synchronized`인가?**
`tryConsume()`과 `refill()`은 read-modify-write 시퀀스다.
`AtomicLong`으로는 두 필드(`tokens`, `lastRefillNanos`)의 복합 갱신을 원자적으로 보장하기 어렵다.
성능보다 정확성이 중요한 상황이므로 `synchronized`가 적합하다.

**가짜 시계(LongSupplier) 주입 이유**
`System.nanoTime()`을 하드코딩하면 "1초 경과 후 토큰 1개 보충" 같은 시나리오를
실제로 1초를 기다리지 않고 테스트할 수 없다.
`LongSupplier`를 생성자로 주입하면 테스트에서 시간을 자유롭게 조작할 수 있다.

```java
// 프로덕션
new TokenBucketRateLimiter(capacity, refillPerSec, System::nanoTime);

// 테스트 (가짜 시계)
long[] now = { 0L };
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 2.0, () -> now[0]);
now[0] += 1_000_000_000L; // 1초 경과
```

---

### Step 2 — ErrorCode 추가

```java
// 들어오는 요청 초과 (우리 서버가 429 반환)
RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),

// 나가는 호출 초과 (우리가 스스로 차단)
OUTBOUND_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "외부 API 호출 한도를 초과했습니다."),

// 토스가 429를 maxAttempts 이상 반환
TOSS_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "결제 서버의 요청 한도를 초과했습니다. 잠시 후 다시 시도해 주세요."),
```

---

### Step 3 — 설정 외부화 (application.yml)

`application.properties`에서 `application.yml`로 전환하거나,
rate limit 설정만 `application.yml`에 추가한다.

```yaml
rate-limit:
  capacity: 10         # 최대 버스트 허용량
  refill-per-sec: 5    # 초당 평균 TPS 상한

outbound-rate-limit:
  capacity: 5
  refill-per-sec: 3

toss:
  retry:
    max-attempts: 3    # 429 재시도 최대 횟수
    fallback-wait-seconds: 1  # Retry-After 헤더 없을 때 대기 시간
```

`@ConfigurationProperties`로 바인딩하면 `@Value`보다 타입 안전하게 관리할 수 있다.

```java
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(long capacity, double refillPerSec) {}
```

---

### Step 4 — RateLimitInterceptor (HandlerInterceptor)

들어오는 요청을 제한한다. 컨트롤러 호출 없이 `preHandle`에서 즉시 거부한다.

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!rateLimiter.tryConsume()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(rateLimiter.retryAfterSeconds()));
            response.getWriter().write("요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.");
            return false;  // 컨트롤러 미호출
        }
        return true;
    }
}
```

**WebMvcConfigurer로 경로 등록:**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/payment/**", "/reservations/**");
    }
}
```

요구사항: "결제·예약 엔드포인트에 적용"

---

### Step 5 — RetryAfterInterceptor (ClientHttpRequestInterceptor)

토스가 429를 반환하면 `Retry-After` 헤더를 파싱해 대기 후 재시도한다.

```java
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final long fallbackWaitSeconds;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ClientHttpResponse response = execution.execute(request, body);

            if (response.getStatusCode().value() != 429) {
                return response;  // 정상 또는 다른 에러 → 상위로 위임
            }

            if (attempt >= maxAttempts) {
                throw new CustomException(ErrorCode.TOSS_RATE_LIMIT_EXCEEDED);
            }

            long waitSeconds = parseRetryAfter(response);
            Thread.sleep(waitSeconds * 1000);
            response.close();
        }

        throw new CustomException(ErrorCode.TOSS_RATE_LIMIT_EXCEEDED);
    }

    private long parseRetryAfter(ClientHttpResponse response) {
        String header = response.getHeaders().getFirst("Retry-After");
        if (header == null) {
            return fallbackWaitSeconds;  // 헤더 없으면 기본값(1초)
        }
        return Long.parseLong(header);
    }
}
```

**핵심 결정사항**

429는 "아직 처리 안 됨" 확정 상태다.
read timeout과 달리 재시도해도 이중 결제 위험이 없다.
그러나 멱등키(`Idempotency-Key: orderId`)는 step2에서 이미 고정 발급되어 있으므로,
재시도 시에도 동일 키가 유지된다.

---

### Step 6 — OutboundRateLimitInterceptor (ClientHttpRequestInterceptor)

나가는 호출이 한도를 초과하면 외부로 보내지 않고 즉시 거부한다.

```java
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new CustomException(ErrorCode.OUTBOUND_RATE_LIMIT_EXCEEDED);
        }
        return execution.execute(request, body);
    }
}
```

**TossPaymentConfig RestClient에 인터셉터 등록:**

```java
return RestClient.builder()
        .requestFactory(factory)
        .baseUrl("https://api.tosspayments.com")
        .defaultHeader("Authorization", "Basic " + encoded)
        .defaultHeader("Content-Type", "application/json")
        .requestInterceptors(interceptors -> {
            interceptors.add(outboundRateLimitInterceptor);  // 먼저: 보내기 전 자체 한도 확인
            interceptors.add(retryAfterInterceptor);          // 다음: 토스의 429 처리
        })
        .build();
```

인터셉터 순서가 중요하다.
`outbound` → `retryAfter` 순서여야 "자체 한도 초과 시 외부로 나가지 않음"이 보장된다.

---

## 변경 파일 목록 요약

| 파일 | 변경 유형 | 내용 |
|------|----------|------|
| `application.yml` | 추가/전환 | `rate-limit.*`, `outbound-rate-limit.*`, `toss.retry.*` |
| `ErrorCode.java` | 추가 | `RATE_LIMIT_EXCEEDED`, `OUTBOUND_RATE_LIMIT_EXCEEDED`, `TOSS_RATE_LIMIT_EXCEEDED` |
| `TokenBucketRateLimiter.java` | 신규 | 토큰 버킷 알고리즘 (LongSupplier 시계 주입) |
| `RateLimitInterceptor.java` | 신규 | HandlerInterceptor — 들어오는 요청 429 거부 |
| `RetryAfterInterceptor.java` | 신규 | ClientHttpRequestInterceptor — 토스 429 재시도 |
| `OutboundRateLimitInterceptor.java` | 신규 | ClientHttpRequestInterceptor — 나가는 호출 Rate Limit |
| `WebConfig.java` (또는 기존 설정) | 수정 | `RateLimitInterceptor` 경로 등록 |
| `TossPaymentConfig.java` | 수정 | RestClient에 두 인터셉터 등록 |

---

## 테스트 전략

### TokenBucketRateLimiter — 단위 테스트 (가짜 시계)

```java
long[] now = { 0L };
LongSupplier clock = () -> now[0];
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 2.0, clock);

// capacity만큼만 즉시 통과
assertThat(limiter.tryConsume()).isTrue();  // 3 → 2
assertThat(limiter.tryConsume()).isTrue();  // 2 → 1
assertThat(limiter.tryConsume()).isTrue();  // 1 → 0
assertThat(limiter.tryConsume()).isFalse(); // 0 → 거부

// 1초 경과 후 2개 보충
now[0] += 1_000_000_000L;
assertThat(limiter.tryConsume()).isTrue();  // 2 → 1
assertThat(limiter.tryConsume()).isTrue();  // 1 → 0
assertThat(limiter.tryConsume()).isFalse(); // 거부
```

검증 포인트:
- `capacity` 개까지만 즉시 소비 가능
- 시간 경과 후 `refillPerSec * elapsed`만큼 보충 (capacity 초과 불가)
- `retryAfterSeconds()` → 올림 값 검증
- 동시 요청에서 정확히 `capacity`개만 통과

### RateLimitInterceptor — MockMvc 테스트

```java
// capacity = 1로 설정
// 첫 번째 요청: 200
// 두 번째 요청: 429 + Retry-After 헤더 확인
mockMvc.perform(post("/payment/confirm")...)
    .andExpect(status().isTooManyRequests())
    .andExpect(header().exists("Retry-After"));
```

### RetryAfterInterceptor — MockRestServiceServer 테스트

```java
// 1차: 429 + Retry-After: 1
// 2차: 200 정상 응답
mockServer.expect(requestTo(CONFIRM_URL)).andRespond(
    withStatus(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "1")
);
mockServer.expect(requestTo(CONFIRM_URL)).andRespond(withSuccess(...));
// → 최종 성공 검증

// maxAttempts 초과 시 TOSS_RATE_LIMIT_EXCEEDED 예외 검증
```

### OutboundRateLimitInterceptor — 단위 테스트

```java
// tryConsume() false일 때 execution.execute() 미호출 검증
// CustomException(OUTBOUND_RATE_LIMIT_EXCEEDED) 발생 검증
```

---

## 공부해볼 것

### 1. capacity vs refillPerSec 트레이드오프

`capacity`를 키우면 순간 버스트를 더 허용한다 (ex: 짧은 시간에 10개 요청 가능).
`refillPerSec`을 키우면 지속 처리량(평균 TPS)이 늘어난다.
**들어오는 한도와 나가는 한도를 같은 값으로 두면 안 되는 이유:**
우리의 처리 용량(들어오는)과 토스가 우리에게 허용한 몫(나가는)은 다른 자원이다.
들어오는 요청 하나가 항상 토스 호출 하나로 이어지지 않을 수 있고,
토스의 Rate Limit은 우리 내부 처리량과 무관하게 토스가 정한다.

### 2. 429 재시도 vs read timeout 재시도의 차이

| 구분 | read timeout (step2) | 429 (step3) |
|------|---------------------|-------------|
| 처리 여부 | 모름 (불확실) | 안 됨 (확정) |
| 안전장치 | 멱등키 (이중 승인 방지) | 없어도 안전, 그래도 멱등키 유지 |
| 재시도 시기 | 즉시 (backoff 후) | Retry-After 대기 후 |

### 3. fail-fast vs blocking 대기

나가는 Rate Limit이 초과되면 현재 설계는 즉시 거부(fail-fast)한다.
대안: 토큰이 찰 때까지 `Thread.sleep()`으로 블로킹 대기.
- 블로킹: 요청이 매끄럽게 흘러가지만 스레드를 잡고 있어 스레드 풀 고갈 위험
- fail-fast: 스레드를 즉시 반환하지만 호출자가 에러를 직접 처리해야 함
- Spring의 스레드 모델(tomcat thread per request)에서는 블로킹이 위험하다.

### 4. Rate Limit vs 서킷 브레이커

Rate Limit은 **호출량(throughput)** 을 제어한다 → "초당 N건만 허용".
서킷 브레이커는 **연속 실패(failure rate)** 를 감지한다 → "실패율이 X%를 넘으면 차단".
Rate Limit이 있어도 토스가 지속적으로 5xx를 반환하면 무의미한 호출이 계속된다.
서킷 브레이커(Resilience4j CircuitBreaker)가 이를 보완한다.

### 5. 토큰 버킷 vs 슬라이딩 윈도우 카운터

| 알고리즘 | 버스트 허용 | 구현 복잡도 | 정확도 |
|---------|------------|------------|--------|
| 토큰 버킷 | capacity만큼 | 낮음 | 보통 |
| 고정 윈도우 카운터 | 경계 시점 2배 허용 | 매우 낮음 | 낮음 |
| 슬라이딩 윈도우 로그 | 없음 | 높음 (메모리 많음) | 높음 |
| 슬라이딩 윈도우 카운터 | 소량 | 중간 | 높음 |

토큰 버킷은 구현이 단순하고 버스트를 자연스럽게 허용해 API Rate Limit에 가장 많이 쓰인다.
