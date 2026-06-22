# Step6 — OutboundRateLimitInterceptor 구현 및 TossPaymentConfig 등록

---

## 결론 먼저

> `OutboundRateLimitInterceptor`가 나가는 호출을 outboundRateLimiter로 선점한다.
> 한도 초과 시 `OUTBOUND_RATE_LIMIT_EXCEEDED` 즉시 거부 (fail-fast).
> `TossPaymentConfig` RestClient에 `outbound → retryAfter` 순으로 등록.

---

## 구현

### OutboundRateLimitInterceptor

```java
@Component
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter outboundRateLimiter;

    public OutboundRateLimitInterceptor(@Qualifier("outboundRateLimiter") TokenBucketRateLimiter outboundRateLimiter) {
        this.outboundRateLimiter = outboundRateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!outboundRateLimiter.tryConsume()) {
            throw new CustomException(ErrorCode.OUTBOUND_RATE_LIMIT_EXCEEDED);
        }
        return execution.execute(request, body);
    }
}
```

### TossPaymentConfig — 인터셉터 등록

```java
@Bean
public RestClient tossRestClient(OutboundRateLimitInterceptor outboundRateLimitInterceptor,
                                 RetryAfterInterceptor retryAfterInterceptor) {
    // ...
    return RestClient.builder()
            .requestFactory(factory)
            .baseUrl("https://api.tosspayments.com")
            .defaultHeader(...)
            .requestInterceptors(interceptors -> {
                interceptors.add(outboundRateLimitInterceptor);  // 먼저: 보내기 전 자체 한도 확인
                interceptors.add(retryAfterInterceptor);          // 다음: 토스의 429 처리
            })
            .build();
}
```

---

## 핵심 결정사항

### 왜 outbound → retryAfter 순서인가?

인터셉터는 체인 순서대로 실행된다.
`outboundRateLimitInterceptor`가 먼저 실행되어야 "자체 한도 초과 시 외부로 요청 자체를 보내지 않음"이 보장된다.
순서가 반대라면 토스에 요청을 보낸 뒤 429를 받고 재시도를 시도할 때, 자체 outbound 한도도 모두 소모되는 비효율이 생긴다.

```
요청 → [outbound 한도 확인] → [토스 전송] → [429면 Retry-After 대기 후 재시도] → 응답
```

### fail-fast vs blocking 대기

나가는 Rate Limit이 초과되면 즉시 거부(fail-fast)를 선택했다.

| 전략 | 장점 | 단점 |
|------|------|------|
| fail-fast (현재) | 스레드 즉시 반환, 스레드 풀 안전 | 호출자가 에러 직접 처리 |
| blocking 대기 | 요청 손실 없음 | Tomcat thread per request 모델에서 스레드 고갈 위험 |

Tomcat thread-per-request 모델에서는 blocking 대기가 스레드 풀을 고갈시킬 수 있어 fail-fast가 적합하다.

### inbound vs outbound rate limit을 분리한 이유

들어오는 한도(inbound)와 나가는 한도(outbound)는 서로 다른 자원을 보호한다.

- **inbound**: 우리 서버의 처리 용량 보호
- **outbound**: 토스가 우리에게 허용한 API 호출량 준수

요청 하나가 항상 토스 호출 하나로 이어지지 않을 수 있고 (내부 캐싱, 실패 등),
토스의 rate limit은 토스가 정한 별도의 자원이므로 값을 분리해 관리한다.

---

## 변경 파일 요약

| 파일 | 변경 유형 | 내용 |
|------|----------|------|
| `RateLimitInterceptor.java` | 신규 | HandlerInterceptor — 들어오는 요청 429 거부 |
| `WebConfig.java` | 신규 | WebMvcConfigurer — 인터셉터 경로 등록 |
| `RetryAfterInterceptor.java` | 신규 | ClientHttpRequestInterceptor — 토스 429 재시도 |
| `OutboundRateLimitInterceptor.java` | 신규 | ClientHttpRequestInterceptor — 나가는 호출 Rate Limit |
| `TossPaymentConfig.java` | 수정 | RestClient에 두 인터셉터 등록 |
| `src/test/resources/application.properties` | 신규 | 테스트 환경 rate-limit 무력화 |

---

## 테스트 전략

### OutboundRateLimitInterceptor — 단위 테스트

```java
// tryConsume() false일 때 execution.execute() 미호출 검증
// CustomException(OUTBOUND_RATE_LIMIT_EXCEEDED) 발생 검증
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(0, 0, System::nanoTime);
OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(limiter);

assertThatThrownBy(() -> interceptor.intercept(request, body, execution))
    .isInstanceOf(CustomException.class)
    .extracting(e -> ((CustomException) e).getErrorCode())
    .isEqualTo(ErrorCode.OUTBOUND_RATE_LIMIT_EXCEEDED);
```
