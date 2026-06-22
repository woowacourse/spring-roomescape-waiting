# Step 3 — 설정 외부화 정리

---

## 무엇을 만들었는가

Rate Limit 관련 설정 값을 코드 밖으로 꺼내고,
`TokenBucketRateLimiter` 빈 두 개(inbound/outbound)를 Spring 컨테이너에 등록했다.

```
src/main/resources/application.properties         ← 설정값 추가
src/main/java/.../ratelimit/RateLimitProperties.java
src/main/java/.../ratelimit/OutboundRateLimitProperties.java
src/main/java/.../ratelimit/TossRetryProperties.java
src/main/java/.../config/RateLimitConfig.java
```

---

## 추가된 설정값

```properties
# 들어오는 요청 Rate Limit
rate-limit.capacity=10
rate-limit.refill-per-sec=5

# 나가는 호출 Rate Limit
outbound-rate-limit.capacity=5
outbound-rate-limit.refill-per-sec=3

# 토스 429 재시도
toss.retry.max-attempts=3
toss.retry.fallback-wait-seconds=1
```

코드를 바꾸지 않고 이 값만 바꾸면 Rate Limit 정책이 달라진다.

---

## @ConfigurationProperties vs @Value

기존 코드(`TossPaymentConfig`)는 `@Value`를 사용했다.

```java
// @Value 방식 (기존)
@Value("${toss.connect-timeout}")
private int connectTimeout;

@Value("${toss.read-timeout}")
private int readTimeout;
```

이번엔 `@ConfigurationProperties`를 사용했다.

```java
// @ConfigurationProperties 방식 (이번)
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(long capacity, double refillPerSec) {}
```

| 구분 | `@Value` | `@ConfigurationProperties` |
|------|----------|---------------------------|
| 선언 위치 | 필드마다 | 클래스 하나에 묶음 |
| 관련 설정 묶음 | 흩어짐 | 한 레코드에 집중 |
| 타입 변환 | 제한적 | 자동 (kebab-case → camelCase 등) |
| IDE 지원 | 보통 | 좋음 (annotation processor 추가 시) |
| 유효성 검증 | 불가 | `@Validated` + `@NotNull` 등 가능 |

설정 항목이 2~3개 이상이고 서로 관련이 있으면 `@ConfigurationProperties`가 낫다.

---

## Relaxed Binding — 프로퍼티 키 형식 자동 변환

Spring Boot는 프로퍼티 키와 필드명 형식이 달라도 자동으로 매핑한다.

```properties
rate-limit.refill-per-sec=5   ← properties 파일 (kebab-case)
```

```java
public record RateLimitProperties(long capacity, double refillPerSec) {}
//                                                      ↑ camelCase
```

`refill-per-sec` → `refillPerSec` 자동 변환. 이를 **Relaxed Binding**이라 한다.

지원 형식:
- `refill-per-sec` (kebab-case) ← 권장
- `refillPerSec` (camelCase)
- `REFILL_PER_SEC` (환경변수 형식)
- `refill_per_sec` (언더스코어)

---

## 왜 빈을 두 개로 나눴는가

`TokenBucketRateLimiter` 빈이 두 개 필요하다.
들어오는 요청(inbound)과 나가는 호출(outbound)이 **별도의 버킷**으로 관리돼야 하기 때문이다.

```java
@Bean("inboundRateLimiter")
public TokenBucketRateLimiter inboundRateLimiter(RateLimitProperties properties) {
    return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec(), System::nanoTime);
}

@Bean("outboundRateLimiter")
public TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties) {
    return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec(), System::nanoTime);
}
```

같은 타입의 빈이 두 개이므로 이름으로 구분한다.
나중에 주입할 때는 `@Qualifier`로 어떤 빈인지 지정한다.

```java
// 예: RateLimitInterceptor에서 inbound 버킷만 주입
@Component
public class RateLimitInterceptor {
    public RateLimitInterceptor(@Qualifier("inboundRateLimiter") TokenBucketRateLimiter rateLimiter) { ... }
}
```

---

## 공부해볼 개념들

### 1. @ConfigurationProperties 동작 원리

`@EnableConfigurationProperties(RateLimitProperties.class)` 선언 시:
1. Spring이 `application.properties`에서 `rate-limit.*` 키를 읽는다
2. `RateLimitProperties` 레코드를 생성하면서 값을 주입한다
3. 이 레코드를 스프링 빈으로 등록한다

`@ConfigurationPropertiesScan`을 메인 애플리케이션 클래스에 붙이면
패키지 전체를 스캔해서 자동으로 등록할 수도 있다.

### 2. record를 설정 객체로 쓰는 이유

```java
// 클래스 방식
public class RateLimitProperties {
    private long capacity;
    private double refillPerSec;
    // getter, setter, constructor...
}

// record 방식
public record RateLimitProperties(long capacity, double refillPerSec) {}
```

설정 객체는 한 번 만들어지면 바뀌지 않아야 한다 (불변).
`record`는 불변이고 생성자·getter·equals·hashCode를 자동으로 만들어준다.
Spring Boot 2.6부터 `@ConfigurationProperties`를 record에 적용할 수 있다.

### 3. 같은 타입 빈 여러 개 — @Qualifier

Spring에서 같은 타입의 빈이 두 개 이상이면 주입 시 어느 것인지 알 수 없어 오류가 난다.

```
NoUniqueBeanDefinitionException: expected single matching bean but found 2
```

해결 방법:
- `@Qualifier("빈이름")` — 이름으로 특정
- `@Primary` — 기본값으로 사용할 빈 지정
- `@Bean("이름")` — 빈에 이름 붙이기

이번에는 두 버킷이 **역할이 다르고** 항상 명시적으로 구분해야 하므로
`@Qualifier`가 가장 명확하다.

### 4. 외부화 설정의 가치

하드코딩:
```java
// 코드 안에 정책이 박혀있음
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 5.0, System::nanoTime);
```

외부화:
```properties
rate-limit.capacity=10
rate-limit.refill-per-sec=5
```

외부화하면:
- 코드 수정·재배포 없이 정책 변경 가능
- 환경별로 다른 값 적용 가능 (개발: 100, 운영: 10)
- 운영팀이 코드 몰라도 설정 파일만 보고 조정 가능

Spring Boot에서 환경별 설정:
```
application.properties         ← 공통 기본값
application-dev.properties     ← 개발 환경 오버라이드
application-prod.properties    ← 운영 환경 오버라이드
```

`spring.profiles.active=prod` 실행 시 prod 파일이 기본값을 덮어쓴다.
