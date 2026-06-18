미션 배경
2단계에서 타임아웃과 멱등키로 느린 호출과 중복 승인을 막았다. 이번엔 호출량 상한(Rate Limit) 을 다룬다. 우리 서버는 같은 Rate Limit을 양쪽 방향에서 마주한다. 들어오는 요청이 몰리면 초과분을 429로 거부하고(서버 입장), 우리가 토스를 호출할 땐 상대 한도를 넘지 않도록 우리 호출량을 스스로 조절한다(클라이언트 입장). 그래도 429가 오면 Retry-After를 존중해 백오프 재시도한다. 알고리즘은 하나(토큰 버킷), 경계는 둘이다.

TPS(Transactions Per Second): 초당 처리 트랜잭션 수 — 측정값. Rate Limit: "초당 N건까지" — 정책. 타임아웃이 성공 TPS를 지키는 것은2 단계 학습 테스트에서 측정으로 체감했다 — 이번엔 그 측정값에 정책으로 상한을 건다.
토큰 버킷: capacity(허용 버스트)만큼 토큰이 차 있고 매초 refillPerSec(평균 TPS 상한)개씩 보충된다. 요청은 토큰 1개를 소비하고, 없으면 거부된다.
시작 전 학습 테스트 learning-test-3-ratelimit을 먼저 푼다(TokenBucketRateLimiter, RateLimitInterceptor, RetryAfterInterceptor, OutboundRateLimitInterceptor).

목표
토큰 버킷 기반 Rate Limit을 HandlerInterceptor로 결제·예약 엔드포인트에 적용해, 초과 요청을 컨트롤러 호출 없이 429+Retry-After로 거부한다.
ClientHttpRequestInterceptor로 토스의 429를 가로채 Retry-After 기반 백오프 재시도를 구현한다.
같은 토큰 버킷을 나가는 호출(egress)에도 적용해, 한도를 넘는 호출은 외부로 보내지 않는다. 들어오는/나가는 Rate Limit이 같은 알고리즘·다른 방향임을 설명할 수 있다.
들어오는/나가는 정책(rate-limit.*, outbound-rate-limit.*)을 application.yml로 외부화한다.
요구사항
1. 토큰 버킷 구현
   capacity(허용 버스트)와 refillPerSec(평균 TPS 상한)을 가진 토큰 버킷을 직접 구현한다. 외부 의존성은 쓰지 않는다.

보충은 "마지막 보충 이후 경과 시간 × refillPerSec"로 계산하되 capacity를 넘지 않는다.
tryConsume(): 토큰 ≥1이면 1개 소비 후 통과(true), 없으면 거부(false).
retryAfterSeconds(): 1개가 찰 때까지 필요한 초를 올림(Math.ceil) 으로 반환한다.
시간 의존 로직은 System::nanoTime을 박지 말고 LongSupplier 가짜 시계를 주입해 결정적으로 테스트한다.
동시 요청에서도 정확히 capacity개만 통과하도록 동시성을 안전하게 처리한다.
2. 서버(게이트웨이) 관점 — 한도 초과 요청 거부
   결제·예약 엔드포인트에 토큰 버킷을 HandlerInterceptor로 적용한다. preHandle에서 tryConsume()이 false면 컨트롤러를 호출하지 않고(false 반환), 응답을 429로 세팅하고 Retry-After 헤더에 retryAfterSeconds() 값(초)을 담는다. capacity/refillPerSec는 rate-limit.*로 외부화해 코드 수정 없이 거부 시점을 바꾼다.

3. 클라이언트 관점 — 토스의 429에 백오프 재시도
   토스 호출 RestClient에 ClientHttpRequestInterceptor를 등록한다. 응답이 429이고 시도 횟수가 maxAttempts 미만이면 Retry-After(초)만큼 대기 후 재시도한다. Retry-After가 없으면 짧은 고정 간격(기본 1초)으로 폴백한다. maxAttempts를 넘어도 429면 도메인 예외로 실패시킨다(무한 재시도 금지). 429는 아직 처리되지 않은 상태라 그냥 다시 보내도 안전하지만, 재시도는 2단계에서 도입한 주문당 고정 멱등키를 유지한 채 보낸다(read timeout처럼 "됐는지 모름"인 경우까지 중복 승인을 막는 전제).

4. 클라이언트 관점 — 나가는 호출에 Rate Limit
   2번의 토큰 버킷을 방향만 바꿔 나가는 호출에 적용한다. 한도를 넘겨 호출하면 어차피 429로 거부당하니, 보내기 전에 스스로 조절하는 게 낫다.

3번과 같은 게이트웨이 RestClient에 인터셉터를 하나 더 등록한다(나가는 호출 한 곳에 Rate Limit·백오프가 함께 걸린다).
호출 전 tryConsume()으로 토큰을 소비하고, 없으면 외부로 보내지 않고 OutboundRateLimitException으로 거부한다.
들어오는 쪽(2번)과 똑같은 TokenBucketRateLimiter를 재사용한다. 나가는 한도는 outbound-rate-limit.*로 들어오는 쪽과 분리해 외부화한다.
완료 기준
한도 내 요청은 정상 처리되고, 초과 요청은 429+Retry-After로 거부된다(컨트롤러 미호출). 토큰 버킷은 가짜 시계 주입으로 결정적으로 테스트된다.
들어오는(rate-limit.*)·나가는(outbound-rate-limit.*) 한도 모두 application.yml 값만 바꿔 코드 수정 없이 거부 시점이 달라진다.
토스가 429+Retry-After를 주면 그만큼 대기 후 재시도해 최종 200을 받는다.
Retry-After가 없으면 짧은 고정 간격(기본 1초)으로 폴백해 재시도한다. 재시도가 maxAttempts를 넘으면 도메인 예외로 실패한다.
나가는 호출이 자체 한도를 넘으면 외부로 나가지 않고 거부되며, 토큰이 보충되면 다시 나간다.
Q. PR 링크

답변을 입력하세요...
0/5000자

더 생각해보기
capacity를 키우면 순간 버스트가, refillPerSec를 키우면 평균 처리량이 어떻게 변하는가?
들어오는 한도(우리 처리 용량)와 나가는 한도(상대가 우리에게 허용한 몫)를 늘 같은 값으로 두면 안 되는 이유는?
나가는 쪽은 지금 한도 초과 시 즉시 거부(fail-fast)한다. 거부 대신 토큰이 찰 때까지 대기(블로킹) 시키는 방식과 비교하면 장단점은?(대기는 호출을 매끄럽게 흘리지만 스레드를 잡고 결정적 테스트가 어렵다.)
read timeout(2단계) 재시도와 429 재시도는 무엇이 다른가("이미 처리됐을 수 있음" vs "아직 처리 안 됨")? 각각 어떤 안전장치가 필요한지 멱등성과 연결해 정리해보자.
Rate Limit은 호출량(throughput) 을 다룬다. 외부가 연속으로 실패(5xx·타임아웃)할 때는 양과 무관하게 호출을 끊는 서킷 브레이커가 필요할 수 있다. Rate Limit과 무엇이 다르고 어떻게 보완하는가?
Q