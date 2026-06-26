# 더 생각해보기 — Rate Limit

> 4차(Rate Limit) 미션의 "더 생각해보기" 5개 항목을 각각 정리한다.
> 코드 근거: [`TokenBucketRateLimiter`](../../src/main/java/roomescape/ratelimit/TokenBucketRateLimiter.java),
> [`RateLimitInterceptor`](../../src/main/java/roomescape/ratelimit/RateLimitInterceptor.java)(들어오는),
> [`RetryAfterInterceptor`](../../src/main/java/roomescape/ratelimit/RetryAfterInterceptor.java)(429 재시도),
> [`OutboundRateLimitInterceptor`](../../src/main/java/roomescape/ratelimit/OutboundRateLimitInterceptor.java)(나가는),
> 학습 테스트 [`RateLimitLearningTest`](../../src/test/java/roomescape/learning/RateLimitLearningTest.java).

---

## 1. capacity를 키우면 순간 버스트가, refillPerSec를 키우면 평균 처리량이 어떻게 변하는가?

- **capacity = 순간에 허용하는 버스트의 크기.** 가득 찬 버킷은 한 번에 capacity개까지 통과시킨다.
  capacity를 키우면 짧은 시간에 몰리는 요청을 더 많이 받아주지만, 그만큼 한순간에 시스템(또는 상대 API)에
  실리는 부하의 최대치가 커진다.
- **refillPerSec = 길게 봤을 때의 평균 처리량 상한(평균 TPS).** 버킷이 빈 뒤로는 초당 refillPerSec개씩만
  보충되므로, 장기 평균 통과율은 refillPerSec로 수렴한다.

학습 테스트 `capacityIsBurst`(가득 찬 버킷에서 capacity개만 통과)와 `refillIsAverageThroughput`
(10초간 초당 refillPerSec개로 수렴)에서 둘을 분리해 측정했다. 즉 **capacity는 "얼마나 몰릴 수 있나",
refillPerSec는 "꾸준히 얼마나 흘릴 수 있나"** 를 따로 조절하는 손잡이다.

---

## 2. 들어오는 한도와 나가는 한도를 늘 같은 값으로 두면 안 되는 이유는?

둘은 **의미가 다른 제약**이기 때문이다.

- **들어오는 한도(`rate-limit.*`)** = 우리 서버가 스스로를 보호하려고 정한 *우리의 처리 용량*. 스레드 풀,
  DB 커넥션 등 우리 자원이 감당할 수 있는 양에서 정한다.
- **나가는 한도(`outbound-rate-limit.*`)** = 토스가 *우리에게 허용한 몫*. 우리 사정과 무관하게 상대가 정한다.

우리 처리 용량이 토스 허용량보다 클 수도(우리가 빨라도 토스가 막음), 작을 수도 있다. 한 값으로 묶으면
한쪽 기준이 다른 쪽을 잘못 제한한다. 그래서 [`rate-limit.*`](../../src/main/resources/application.properties)와
`outbound-rate-limit.*`를 분리해 외부화하고, 같은 알고리즘(`TokenBucketRateLimiter`)을 서로 다른 버킷으로 쓴다.

---

## 3. 나가는 쪽 fail-fast(즉시 거부) vs 대기(블로킹) 비교

현재 [`OutboundRateLimitInterceptor`](../../src/main/java/roomescape/ratelimit/OutboundRateLimitInterceptor.java)는
토큰이 없으면 즉시 `OutboundRateLimitException`으로 거부한다(fail-fast).

| | fail-fast(즉시 거부) | 대기(블로킹) |
|---|---|---|
| 호출 흐름 | 끊김 — 호출부가 재시도/대체 처리 | 매끄럽게 흘러감(셰이핑) |
| 스레드 | 즉시 반환, 점유 없음 | 토큰 찰 때까지 스레드 점유 → 풀 고갈 위험 |
| 지연 | 빠른 실패(예측 가능) | 숨은 지연(얼마나 기다릴지 불명확) |
| 테스트 | 결정적 | 시간 의존, 결정적 테스트 어려움 |

대기는 버스트를 부드럽게 흘려보내는 장점이 있지만, 스레드를 잡아 2단계에서 본 스레드 고갈 문제로 이어질 수
있고 결정적 테스트가 어렵다. 결제처럼 빠른 실패와 명확한 안내가 중요한 경로에서는 fail-fast가 더 안전하다.

---

## 4. read timeout(2단계) 재시도와 429 재시도는 무엇이 다른가?

핵심은 **"이미 처리됐을 수 있음" vs "아직 처리 안 됨"** 이다.

| | read timeout (2단계) | 429 (4단계) |
|---|---|---|
| 상태 | 토스에 닿았으나 응답 못 받음 → **됐는지 모름** | 토스가 받지 않고 거절 → **아직 처리 안 됨** |
| 단순 재시도 | 위험(이미 승인됐다면 이중 승인) | 안전(처리되지 않았으므로) |
| 필요한 안전장치 | **멱등키 필수** — 같은 키로 보내야 이중 승인 방지 | 그냥 다시 보내도 안전 |

429는 그냥 다시 보내도 안전하지만, **재시도는 read timeout 같은 불확실성까지 함께 막기 위해 2단계의
주문당 고정 Idempotency-Key를 유지한 채** 보낸다(요구사항 3). 즉 멱등키는 "됐는지 모름"을 안전하게 만드는
공통 전제이고, 429 재시도도 그 위에서 동작한다.

---

## 5. Rate Limit과 서킷 브레이커는 무엇이 다르고 어떻게 보완하는가?

- **Rate Limit은 호출량(throughput)을 다룬다.** "초당 N건"이라는 양의 상한. 상대가 정상이어도 한도를 넘으면 막는다.
- **서킷 브레이커는 건강 상태(failure)를 다룬다.** 외부가 연속으로 실패(5xx·타임아웃)하면, 양과 무관하게
  잠시 호출 자체를 끊어 빠르게 실패시키고 회복 시간을 준다.

둘은 끊는 기준이 다르다(양 vs 실패율). Rate Limit만 있으면, 토스가 다운돼 매 호출이 타임아웃으로 실패하는데도
한도 안이라는 이유로 계속 호출해 스레드와 시간을 낭비한다. 이때 서킷 브레이커가 회로를 열어 호출을 끊어준다.
반대로 서킷 브레이커만 있으면 정상 상태의 과도한 호출량을 막지 못한다. **Rate Limit(평소의 양 제어) +
서킷 브레이커(장애 시 차단)** 가 서로를 보완한다.
