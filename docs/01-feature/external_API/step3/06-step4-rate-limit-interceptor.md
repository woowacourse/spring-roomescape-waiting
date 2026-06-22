# Step4 — RateLimitInterceptor (HandlerInterceptor) 구현

---

## 결론 먼저

> `RateLimitInterceptor.preHandle()`에서 inboundRateLimiter를 소비한다.
> 토큰이 없으면 429 + `Retry-After` 헤더를 직접 써서 응답하고 `false` 반환.
> `WebConfig.addInterceptors()`로 `/payment/**`, `/reservations/**`에 등록.

---

## 구현

### RateLimitInterceptor

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter inboundRateLimiter;

    public RateLimitInterceptor(@Qualifier("inboundRateLimiter") TokenBucketRateLimiter inboundRateLimiter) {
        this.inboundRateLimiter = inboundRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!inboundRateLimiter.tryConsume()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            response.setHeader("Retry-After", String.valueOf(inboundRateLimiter.retryAfterSeconds()));
            response.getWriter().write("{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}");
            return false;
        }
        return true;
    }
}
```

### WebConfig

```java
@Configuration
@Import(RateLimitConfig.class)
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/payment/**", "/reservations/**");
    }
}
```

---

## 핵심 결정사항

### 왜 CustomException을 던지지 않고 response에 직접 쓰는가?

`preHandle`에서 `CustomException`을 던지면 `GlobalExceptionHandler`가 JSON 본문을 쓰지만,
`Retry-After` 헤더는 `ResponseEntity`에 포함되지 않는다.
interceptor 단에서 헤더 + 본문을 함께 제어하려면 `HttpServletResponse`에 직접 쓰는 것이 가장 단순하다.

### 왜 @Import(RateLimitConfig.class)를 WebConfig에 추가했는가?

`@WebMvcTest`는 `WebMvcConfigurer` 구현체를 자동 로드하지만, `@Configuration` 빈(RateLimitConfig)은 로드하지 않는다.
`WebConfig`가 `RateLimitInterceptor`를 생성자 주입받고, `RateLimitInterceptor`가 `inboundRateLimiter` 빈을 필요로 하기 때문에,
`@WebMvcTest` 슬라이스 테스트에서 컨텍스트 로드 실패가 발생했다.
`@Import(RateLimitConfig.class)`를 추가하면 `WebConfig` 로드 시 `RateLimitConfig`도 함께 등록되어 문제가 해결된다.

### 테스트 환경 rate-limit 설정 분리

`src/test/resources/application.properties`에 `rate-limit.capacity=100000`을 설정해
단위·인수 테스트가 rate limit에 걸리지 않도록 분리한다.
프로덕션 설정(`rate-limit.capacity=10`)은 `src/main/resources/application.properties`에 유지한다.

---

## 테스트 전략

### MockMvc 테스트 (capacity=1 설정)

```java
// capacity = 1로 설정
// 첫 번째 요청: 200 (또는 해당 엔드포인트의 정상 코드)
// 두 번째 요청: 429 + Retry-After 헤더 확인
mockMvc.perform(post("/payment/confirm")...)
    .andExpect(status().isTooManyRequests())
    .andExpect(header().exists("Retry-After"));
```
