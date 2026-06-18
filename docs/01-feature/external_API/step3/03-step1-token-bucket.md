# Step 1 — TokenBucketRateLimiter 구현 정리

---

## 무엇을 만들었는가

토큰 버킷(Token Bucket) 알고리즘을 직접 구현했다.
외부 라이브러리 없이, 가짜 시계를 주입받아 결정적으로 테스트 가능한 Rate Limiter다.

```
src/main/java/roomescape/global/ratelimit/TokenBucketRateLimiter.java
src/test/java/roomescape/global/ratelimit/TokenBucketRateLimiterTest.java
```

---

## 토큰 버킷 알고리즘이란

**비유**: 양동이(버킷)에 토큰(동전)이 담겨 있다.
- 요청이 오면 토큰 1개를 꺼낸다 → 요청 통과
- 토큰이 없으면 요청을 거부한다
- 시간이 지나면 토큰이 일정 속도로 다시 채워진다

**두 개의 파라미터가 의미하는 것:**

| 파라미터 | 역할 | 비유 |
|---------|------|------|
| `capacity` | 버킷 크기 (최대 보유 토큰 수) | 양동이 크기 → 순간 버스트 허용량 |
| `refillPerSec` | 초당 보충 속도 | 수도꼭지 속도 → 평균 TPS 상한 |

```
capacity = 10, refillPerSec = 5 이라면:

초기: 토큰 10개 (버스트: 순간에 10개 요청 가능)
1초 후: 빈 상태에서 5개 보충 (지속 처리량: 초당 5건)
2초 후: 5개 → 10개 (capacity 상한)
```

---

## 코드 구조 설명

```java
public class TokenBucketRateLimiter {

    private final long capacity;       // 양동이 크기
    private final double refillPerSec; // 초당 보충 속도
    private final LongSupplier clock;  // 시계 (나노초 단위)

    private double tokens;             // 현재 토큰 수
    private long lastRefillNanos;      // 마지막 보충 시각
```

### tryConsume() — 토큰 소비

```java
public synchronized boolean tryConsume() {
    refill();           // 1. 경과 시간만큼 먼저 보충
    if (tokens >= 1.0) {
        tokens -= 1.0;  // 2. 토큰 1개 소비
        return true;    // 3. 통과
    }
    return false;       // 4. 토큰 없으면 거부
}
```

### refill() — 보충 계산

```java
private void refill() {
    long now = clock.getAsLong();
    double elapsed = (now - lastRefillNanos) / 1_000_000_000.0; // 경과 초
    tokens = Math.min(capacity, tokens + elapsed * refillPerSec); // 보충 (capacity 초과 불가)
    lastRefillNanos = now;
}
```

경과 시간(초) × 초당 보충 속도 = 보충할 토큰 수

### retryAfterSeconds() — 대기 시간 계산

```java
public synchronized long retryAfterSeconds() {
    refill();
    double secondsNeeded = (1.0 - tokens) / refillPerSec;
    return (long) Math.ceil(secondsNeeded); // 올림
}
```

"1개가 차려면 얼마나 기다려야 하는가"를 초 단위 올림으로 반환한다.
클라이언트에게 `Retry-After` 헤더로 내려주는 값이다.

---

## 왜 이렇게 설계했는가

### 1. `double tokens` — 왜 정수가 아닌가

`long`으로 정수 토큰만 관리하면 보충 시 소수점이 버려진다.

```
refillPerSec = 2.0, 0.4초마다 tryConsume() 호출

long 방식:
  0.4초 경과 → 0.4 * 2.0 = 0.8 → 정수로 버리면 0
  0.4초 경과 → 또 0.8 → 정수로 버리면 0
  → 영원히 토큰이 안 생김 (버그!)

double 방식:
  0.4초 경과 → tokens += 0.8 → 0.8
  0.2초 경과 → tokens += 0.4 → 1.2 → 통과 가능
  → 정확하게 동작
```

### 2. `synchronized` — 왜 AtomicLong이 아닌가

`tokens`와 `lastRefillNanos` 두 필드를 동시에 일관되게 바꿔야 한다.

```
tryConsume()의 실행 순서:
  1. refill() → lastRefillNanos 읽기
  2. tokens 계산
  3. tokens 수정
  4. lastRefillNanos 수정

AtomicLong으로는 두 필드의 복합 갱신을 원자적으로 보장할 수 없다.
스레드 A가 1~2 사이에 끼어들면 잘못된 값으로 계산할 수 있다.
→ synchronized로 메서드 전체를 임계 구역으로 잠근다.
```

### 3. `LongSupplier clock` — 왜 System.nanoTime()을 직접 안 쓰는가

`System.nanoTime()`을 하드코딩하면 "1초 경과 후 2개 보충"을 테스트하려면
실제로 1초를 기다려야 한다. 테스트가 느려지고 불안정해진다.

```java
// 하드코딩 — 테스트 불가능
private long lastRefillNanos = System.nanoTime();

// 주입 — 가짜 시계로 테스트 가능
private final LongSupplier clock;
private long lastRefillNanos = clock.getAsLong();
```

테스트에서는 `long[] now = {0L}`으로 가짜 시계를 만들고,
`now[0] += 1_000_000_000L`으로 1초를 즉시 경과시킨다.

---

## 테스트 설계 포인트

### 가짜 시계 패턴

```java
long[] now = {0L};                              // 배열로 감싸야 람다 안에서 수정 가능
TokenBucketRateLimiter limiter =
    new TokenBucketRateLimiter(5, 2.0, () -> now[0]);

now[0] += 1_000_000_000L; // 1초 경과
```

**왜 `long[]` 배열인가?**
Java 람다에서 외부 변수를 캡처하려면 `effectively final`이어야 한다.
`long now`는 `now++`로 수정할 수 없지만,
`long[] now`는 배열 자체는 final이고 내부 값(`now[0]`)은 변경 가능하다.

### 동시성 테스트 패턴 — CountDownLatch

```java
CountDownLatch startLatch = new CountDownLatch(1); // 출발 신호탄
CountDownLatch doneLatch  = new CountDownLatch(20); // 완료 대기

// 20개 스레드 준비
executor.submit(() -> {
    startLatch.await(); // 출발 신호 대기
    if (limiter.tryConsume()) successCount.incrementAndGet();
    doneLatch.countDown();
});

startLatch.countDown(); // 동시 출발
doneLatch.await();      // 전원 완료 대기
```

- `CountDownLatch(1)`: 신호탄 역할. `countDown()` 한 번에 대기 중인 스레드 전체 깨움
- `CountDownLatch(20)`: 완료 카운터. 20개 스레드가 다 끝날 때까지 메인 스레드 대기
- `AtomicInteger successCount`: 스레드 간 공유 카운터. `++` 연산이 원자적

---

## 공부해볼 개념들

### 1. Rate Limit 알고리즘 비교

| 알고리즘 | 원리 | 버스트 허용 | 구현 난이도 |
|---------|------|------------|------------|
| **토큰 버킷** (이번 구현) | 토큰 소비/보충 | O (capacity만큼) | 낮음 |
| 고정 윈도우 카운터 | N초마다 카운터 리셋 | 경계 시점에 2배 허용 | 매우 낮음 |
| 슬라이딩 윈도우 로그 | 요청 시각 목록 유지 | X | 높음 (메모리 많음) |
| 슬라이딩 윈도우 카운터 | 이전 윈도우 비율 반영 | 소량 | 중간 |

토큰 버킷이 API Rate Limit에 가장 많이 쓰이는 이유:
- 버스트를 자연스럽게 허용 (capacity)
- 평균 처리량을 보장 (refillPerSec)
- 구현이 단순

### 2. 동시성 제어 기법

이번에 `synchronized`를 사용했지만, Java에는 다양한 동시성 도구가 있다.

**synchronized 키워드**
```java
public synchronized boolean tryConsume() { ... }
// 이 메서드를 호출하는 스레드는 객체의 모니터 락을 획득
// 한 번에 한 스레드만 진입 가능
```

**대안들:**
- `ReentrantLock`: synchronized보다 유연 (타임아웃, 조건 변수 등)
- `AtomicLong`: 단일 숫자의 원자적 연산에 적합 (복합 연산엔 부적합)
- `StampedLock`: 읽기/쓰기 분리가 필요할 때 (읽기가 압도적으로 많은 경우)

**synchronized의 한계:**
- 스레드가 락을 기다리는 동안 블로킹 (CPU는 놀고 있음)
- 락 경합이 심할 때 성능 저하
- 하지만 Rate Limiter처럼 "정확성이 성능보다 중요"한 경우엔 적합

### 3. Java 시간 API

이번 구현에서 `System.nanoTime()`을 사용하는 이유:

| API | 단위 | 용도 |
|-----|------|------|
| `System.currentTimeMillis()` | ms | 벽시계 시간 (날짜/시각) |
| `System.nanoTime()` | ns | 경과 시간 측정 (절대값 의미 없음) |
| `Instant.now()` | ns | 벽시계 + 나노초 정밀도 |

Rate Limiter는 "몇 시인지"가 아니라 "얼마나 지났는지"가 중요하므로
`System.nanoTime()`이 적합하다.

`nanoTime()`은 절대값이 의미 없고 두 값의 차이만 의미 있다.
그래서 `now - lastRefillNanos`로 경과 시간을 계산한다.

### 4. 함수형 인터페이스 — LongSupplier

`LongSupplier`는 Java 표준 함수형 인터페이스다.

```java
@FunctionalInterface
public interface LongSupplier {
    long getAsLong();
}
```

`Supplier<Long>`과 비슷하지만 오토박싱(Long ↔ long) 비용이 없다.
`System::nanoTime`은 `long`을 반환하는 메서드이므로 `LongSupplier`에 딱 맞는다.

```java
LongSupplier realClock  = System::nanoTime;   // 프로덕션
LongSupplier fakeClock  = () -> now[0];        // 테스트
```

### 5. 테스트에서 외부 의존성을 주입하는 패턴

이번에 배운 핵심 패턴: **시간을 주입 가능하게 만들기**

시간, 난수, 외부 API처럼 "테스트에서 제어하기 어려운 것"은
생성자로 주입받으면 테스트에서 가짜로 교체할 수 있다.

```java
// 제어 불가능한 코드
public class RateLimiter {
    private long lastRefill = System.nanoTime(); // 하드코딩
}

// 주입으로 제어 가능
public class RateLimiter {
    private final LongSupplier clock;
    private long lastRefill;

    public RateLimiter(LongSupplier clock) {
        this.clock = clock;
        this.lastRefill = clock.getAsLong();
    }
}
```

이 패턴은 스프링의 DI(의존성 주입)와 같은 원리다.
의존하는 것을 외부에서 주입받으면 테스트가 쉬워지고 결합도가 낮아진다.
