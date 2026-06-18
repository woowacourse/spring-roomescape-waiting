# Outbound Rate Limit

## 목적
Toss Payments 승인 API처럼 외부로 나가는 호출도 우리 서버가 직접 속도를 조절한다. 한도를 넘긴 요청은 Toss에 보내도 429로 거부될 가능성이 높으므로, 보내기 전에 내부에서 차단해 외부 API와 우리 서버 스레드를 같이 보호한다.

## 적용 위치
`TossClientConfig`에서 Toss 전용 `RestClient`에 두 인터셉터를 등록했다.

```java
.requestInterceptor(new TossOutboundRateLimitInterceptor(outboundTokenBucket))
.requestInterceptor(new TossRetryInterceptor(maxAttempts))
```

이 구성에서는 결제 승인 confirm 호출이 Toss 호출 흐름에 들어가기 전에 outbound 토큰을 먼저 확인한다. 429 응답 이후의 백오프와 재시도 횟수 제한은 기존 `TossRetryInterceptor`가 담당한다.

## 동작 방식
`TossOutboundRateLimitInterceptor`는 요청 전 `TokenBucket.tryConsume()`을 호출한다.

- 토큰이 있으면 1개를 소비하고 Toss로 요청을 보낸다.
- 토큰이 없으면 Toss로 요청을 보내지 않고 `OutboundRateLimitException`을 던진다.
- `OutboundRateLimitException`은 `429 Too Many Requests`로 매핑된다.
- 다음 시도까지 필요한 시간은 `TokenBucket.retryAfterSeconds()`로 계산해 예외에 보관한다.

## 설정
들어오는 요청 제한과 나가는 요청 제한은 서로 다른 prefix를 쓴다.

```properties
rate-limit.capacity=10.0
rate-limit.refill-per-sec=2.0

outbound-rate-limit.capacity=30.0
outbound-rate-limit.refill-per-sec=10.0
```

`rate-limit.*`는 서버로 들어오는 예약/결제 요청 제한이고, `outbound-rate-limit.*`는 Toss로 나가는 결제 승인 요청 제한이다. 두 값을 분리해야 사용자 트래픽 제한과 외부 PG 호출 제한을 독립적으로 조정할 수 있다.

## 재사용한 구성
새 알고리즘을 만들지 않고 기존 `TokenBucket`을 재사용했다.

- inbound: `RateLimitInterceptor` + `tokenBucket`
- outbound: `TossOutboundRateLimitInterceptor` + `outboundTokenBucket`

둘 다 같은 토큰 버킷 알고리즘을 쓰지만 빈과 프로퍼티는 분리되어 있다.

## 테스트
`TossOutboundRateLimitInterceptorTest`에서 다음을 확인한다.

- 토큰이 없으면 Toss로 요청을 보내기 전에 차단한다.
- 토큰이 있으면 정상적으로 외부 요청을 보낸다.
- 시간이 지나 토큰이 보충되면 다시 요청을 보낼 수 있다.
