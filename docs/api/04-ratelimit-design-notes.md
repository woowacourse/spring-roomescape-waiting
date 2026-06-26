# Rate Limit 설계 노트 — 트레이드오프와 특이점

> 4차(Rate Limit) 구현에서 미션 문구 그대로가 아니라 **이 프로젝트의 설계·제약에 맞춰 바꾼 지점**과,
> 동작은 맞지만 **의식해 둘 트레이드오프**를 정리한다. 요구사항 충족 여부는 [CHANGES.md](../../CHANGES.md) §18~22,
> 개념 정리는 [03-ratelimit-think-more.md](03-ratelimit-think-more.md) 참고.

---

## 1. 설계/제약에 따라 바뀐 특이점

### 1-1. 인바운드 인터셉터를 `@Bean MappedInterceptor`로 등록
**파일**: [`config/RateLimitConfig.java`](../../src/main/java/roomescape/config/RateLimitConfig.java)

미션 문구는 "HandlerInterceptor"라 처음엔 `WebMvcConfigurer.addInterceptors`로 등록했다. 그런데 그
구성 클래스의 **생성자 `@Value`가 placeholder 해결 전에 인스턴스화돼 `NumberFormatException`**으로 컨텍스트가
깨졌고(`${rate-limit.capacity}`가 문자열 그대로 들어옴), 그 여파로 `@WebMvcTest` 슬라이스의 컨트롤러 테스트가
전부 동반 실패했다.

→ `@Bean MappedInterceptor`로 바꿨다. `@Bean` 메서드 파라미터 `@Value`는 빈 생성 시점(placeholder 해결 후)에
주입되어 정상 동작하고, `@WebMvcTest`는 일반 `@Configuration`을 로드하지 않으므로 슬라이스도 비침투다.
인터셉터 본체 [`RateLimitInterceptor`](../../src/main/java/roomescape/ratelimit/RateLimitInterceptor.java)는
그대로 `HandlerInterceptor`다(등록 방식만 바뀜).

### 1-2. `application.properties`로 외부화 (yml 아님)
미션은 `application.yml`을 명시했지만 레포 전체가 `.properties`라 기존 양식을 따랐다. 외부화의 본질
("코드 수정 없이 값만 바꿔 거부 시점이 달라진다")은 동일하게 만족한다. 추가로
**`src/test/resources/application.properties`가 메인을 가리는 구조**라 같은 키를 양쪽에 넣어야 했다.

### 1-3. 단일 전역 버킷
per-IP/per-user 키 구분 없이 전역 버킷 하나. 미션이 키별 구분을 요구하지 않아 가장 단순한 형태를 택했다.
한계는 1-2장 트레이드오프 참고. 적용 경로는 `/reservations`·`/payments`(하위 포함), GET 읽기까지 포함.

### 1-4. egress 예외를 429가 아닌 503으로 매핑
**파일**: [`exception/ProblemDetailsAdvice.java`](../../src/main/java/roomescape/exception/ProblemDetailsAdvice.java)

429는 "우리 인바운드 한도" 전용으로 두고, 다운스트림(토스) 관련 throttle은 503+`Retry-After`로 일관화했다.
- `TOSS_RATE_LIMITED` (토스가 우리를 429로 막고 재시도 소진) → 503
- `PAYMENT_OUTBOUND_RATE_LIMITED` (우리 egress 한도 자체 거부) → 503

### 1-5. 인터셉터 순서: retry(바깥) → outbound-limit(안쪽)
**파일**: [`config/TossClientConfig.java`](../../src/main/java/roomescape/config/TossClientConfig.java)

재시도마다 실제 전송이 일어나므로 **재시도 1회당 outbound 토큰 1개를 소비**하도록 의도적으로 배치했다
("실제 전송 = 토큰 소비" 원칙). 순서를 반대로 두면 "논리적 호출 1건당 토큰 1개"가 되지만 재시도가 한도를 우회한다.

### 1-6. 기본값이 테스트 제약의 영향을 받음
인바운드 기본 `capacity=20`은 기존 기능 테스트(엔드포인트 호출 ≤3건)가 throttle되지 않게 한 값이기도 하다.
실제 429 입증은 [`RateLimitIntegrationTest`](../../src/test/java/roomescape/integration/RateLimitIntegrationTest.java)에서
`@TestPropertySource(rate-limit.capacity=2)+@DirtiesContext`로 분리했다.
[`TossPaymentGatewayTest`](../../src/test/java/roomescape/payment/gateway/toss/TossPaymentGatewayTest.java)는
~13회 연속 호출이라 `outbound-rate-limit.capacity=1000`을 오버라이드해 그 테스트가 자기 한도에 막히지 않게 했다
(레이트리밋 자체를 검증하는 테스트가 아니므로).

---

## 2. 의식해 둘 트레이드오프

### 2-1. 🔴 2단계 타임아웃 ↔ 3단계 재시도의 긴장
**파일**: [`ratelimit/RetryAfterInterceptor.java`](../../src/main/java/roomescape/ratelimit/RetryAfterInterceptor.java)

2단계 타임아웃의 목적은 느린 호출에서 **스레드를 빨리 놓아주는 것**이었는데, 3단계 백오프 재시도는
`Thread.sleep`으로 **그 스레드를 다시 붙잡는다**. 한 결제 요청이 최악의 경우
`read-timeout + (maxAttempts-1) × Retry-After`만큼 스레드를 점유할 수 있다. 동기 `RestClient`라 블로킹이
불가피하지만, 부하 시 스레드 풀 관점에서 두 단계가 상충한다는 점을 인지해야 한다. (비동기 호출이나
재시도 상한·전체 시간 예산(budget)으로 완화 가능 — 향후 과제)

### 2-2. 🟠 retry와 outbound-limit의 복합 실패
토스가 우리를 429로 강하게 막는 상황이면, 재시도가 outbound 토큰까지 소비해 **우리 자신의 outbound 한도에도
걸릴 수** 있다(이중 방어이지만 실패가 겹칠 수 있음). 1-5의 순서 선택("실제 전송 = 토큰 소비")의 대가다.

### 2-3. 🟠 전역 버킷의 형평성
한 클라이언트가 공유 버킷을 소진하면 무관한 사용자까지 429를 받는다. per-key 버킷(`Map<key, bucket>` + 만료)으로
격리할 수 있지만 메모리·만료 관리 비용이 생긴다. 현재 규모에선 전역 버킷이 합리적이나, 멀티테넌시로 가면 재검토 대상.

### 2-4. 🟢 egress는 fail-fast (대기 아님)
**파일**: [`ratelimit/OutboundRateLimitInterceptor.java`](../../src/main/java/roomescape/ratelimit/OutboundRateLimitInterceptor.java)

블로킹 대기 대신 즉시 거부를 택해 스레드 점유·비결정적 테스트를 피했다. 대신 호출 흐름이 끊겨 호출부가
재시도·안내를 책임진다(`Retry-After`로 언제 다시 시도할지 전달). 자세한 비교는
[03-ratelimit-think-more.md §3](03-ratelimit-think-more.md) 참고.

### 2-5. ⚪ Rate Limit ≠ 서킷 브레이커
Rate Limit은 호출량을 다루고, 연속 실패(5xx·타임아웃)에 대한 차단은 다루지 못한다. 토스가 다운돼 매 호출이
타임아웃으로 실패해도 한도 안이면 계속 호출한다. 이 공백은 서킷 브레이커가 보완한다 —
[03-ratelimit-think-more.md §5](03-ratelimit-think-more.md). 2-1의 스레드 점유 문제와도 연결되는 다음 과제.
