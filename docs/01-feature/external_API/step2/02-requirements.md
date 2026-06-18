미션 배경
1단계에서 붙인 토스 호출에는 타임아웃 방어가 없다. 토스가 느리면 그 한 번의 호출이 우리 스레드를 무한정 붙잡아 스레드 풀을 고갈시키고, 결제와 무관한 요청까지 멈춘다. 또 read timeout으로 실패한 결제는 "안 된 것"이 아니라 "됐는지 모르는 것" 이다. 토스 쪽에서는 이미 승인이 끝났을 수도 있어, 무작정 재시도하면 이중 승인이 난다. 이 단계는 기존 코드 위에 타임아웃과 멱등 재시도라는 방어를 더한다.

시작 전 학습 테스트 learning-test-2-timeout을 먼저 푼다. MockWebServer로 느린 서버를 흉내 내, connect/read timeout을 걸었을 때 얼마나 기다렸다 어떤 예외로 실패하는지를 경과 시간 단언으로 체감하는 모듈이다. 느린 호출을 일찍 포기해야 성공 TPS(초당 성공 트랜잭션)가 유지되는 것도 측정으로 확인한다.

목표
토스 호출 RestClient에 connect/read timeout을 설정해, 느린 호출이 우리 스레드를 무한정 붙잡지 못하게 한다.
타임아웃·연결 실패가 어떤 예외로 표면화되는지 이해하고, 특히 결과가 불명확한 read timeout을 토스 에러와 구분해 처리한다.
confirm에 Idempotency-Key(주문당 고정 UUID)를 도입해, 타임아웃 후 재시도나 success 페이지 새로고침으로 같은 결제가 중복 승인되는 일을 막는다.
사용자가 자신의 주문·결제 상태를 확인할 수 있는 주문/결제 내역 페이지를 제공한다.
요구사항
1. 토스 호출 RestClient에 타임아웃 설정
   1단계의 RestClient(또는 그 Builder)에 connect timeout(연결까지 기다리는 최대 시간)과 read timeout(연결 후 응답을 읽기까지 기다리는 최대 시간)을 설정한다. 둘 중 한 방식을 고른다.

프로퍼티 방식: Spring Boot 4.0의 복수형 prefix spring.http.clients.*(connect-timeout, read-timeout, imperative.factory). 단수 spring.http.client.*는 deprecated다.
코드 방식: SimpleClientHttpRequestFactory에 setConnectTimeout/setReadTimeout을 호출하고 RestClient.builder().requestFactory(factory)로 연결한다.
요청 팩토리는 simple(또는 apache)을 쓴다. jdk는 응답 바디 지연을 read timeout으로 못 잡는 한계가 있다. 타임아웃 값은 코드에 박지 말고 application.yml로 외부화한다.

2. 타임아웃·연결 실패 예외를 결제 흐름에서 처리
   타임아웃·연결 실패는 체크 예외(SocketTimeoutException/ConnectException)지만 RestClient가 unchecked로 감싸 던지며, root cause에 원래의 java.net 예외가 들어 있다.

상황	표면화 예외	근본 원인
연결 단계 실패(거부/연결 타임아웃)	ResourceAccessException	ConnectException / SocketTimeoutException
응답 읽기 단계 실패(느린 응답)	RestClientException	SocketTimeoutException
이 둘을 1단계의 토스 에러 응답({code, message})과 구분해 사용자에게 적절히 안내한다. 토스 에러는 "거절", 타임아웃은 "답 없음"이다. 특히 read timeout은 "승인됐는지 모르는" 상태이므로 "결제 실패"라고 단정하지 말고, 결과 확인·재시도가 가능하도록 처리한다. 결과가 불명확한 실패를 성공/실패 둘 중 하나로 성급히 결론짓지 않는 것이 핵심이다.

3. Idempotency-Key로 안전한 재시도 보장
   confirm은 POST라 멱등성이 자동 보장되지 않으므로 Idempotency-Key 헤더로 직접 보장한다.

주문 생성 시 주문당 고정 UUID를 멱등키로 만들어 저장한다(재시도해도 같은 주문엔 항상 같은 키, ≤300자·15일 유효).
confirm 호출에 이 키를 헤더로 보낸다. 같은 키로 다시 호출하면 토스가 첫 응답을 그대로 반환하므로, 타임아웃 후 재시도나 success 새로고침으로 중복 호출돼도 이중 승인되지 않는다. 1단계의 ALREADY_PROCESSED_PAYMENT 처리와 함께 두 겹의 방어가 된다.
매 호출마다 새 UUID를 만들면 멱등성이 무의미하다. 키는 주문에 고정한다.

4. 주문/결제 내역 페이지 (결제 내역 확인)
   사용자가 자신의 주문과 결제 상태를 한눈에 확인할 수 있는 페이지를 만든다(미션 요약의 "'내 예약' 페이지에서 결제 관련 정보를 예약 정보와 함께 확인할 수 있다" 가 여기서 구현된다).

로그인한 사용자의 주문(예약) 목록에 예약 정보(날짜·시간·테마 등)와 함께 결제 상태(결제 대기 / 확정(CONFIRMED) / 실패), orderId, 승인됐다면 1단계에서 저장한 paymentKey와 금액을 표시한다.
read timeout처럼 결과가 불명확한 경우는 "결제 실패"로 단정하지 말고 "확인 필요" 로 구분해 보여준다(요구사항 2). 이런 주문의 안전한 재시도는 요구사항 3의 멱등키로 보장되므로, 다시 confirm해도 이중 승인되지 않는다.
완료 기준
토스가 느리게 응답해도 설정한 read timeout만큼만 기다린 뒤 실패하고, 결제 흐름이 무한 대기에 빠지지 않는다.
연결 실패는 ResourceAccessException, 느린 응답은 RestClientException으로 표면화되며, 토스 에러 응답과 구분되어 처리된다.
read timeout처럼 결과가 불명확한 실패가 "결제 실패"로 성급히 단정되지 않고, 재확인·재시도 가능한 상태로 처리된다.
같은 주문의 confirm 재요청(타임아웃 후 재시도, success 새로고침)이 Idempotency-Key로 중복 승인되지 않는다.
주문/결제 내역 페이지에서 예약 정보와 결제 상태(대기/확정/확인 필요), orderId·paymentKey·금액을 확인할 수 있다.
Q. PR 링크

답변을 입력하세요...
0/5000자

더 생각해보기
read timeout 값과 토스 응답 시간이 거의 같은 경계값에서는 성공과 실패가 어떻게 갈릴까? 이때 얻은 감각을 connect/read timeout 값을 정하는 데 어떻게 활용할 수 있을까?
결제 승인 호출에서 connect와 read 중 어느 쪽을 더 짧게 두는 게 합리적일까?
read timeout으로 불확실해진 결제를 "확인 필요"로 두는 것과 "실패"로 단정하는 것 중 무엇이 나을까? "확인 필요"가 영구 상태로 방치되지 않고 성공/실패로 수렴하려면 무엇이 필요할까(결제 조회 API, 멱등 재시도)?
read timeout(됐는지 모름)과 connect 실패(연결조차 못 함)는 재시도 안전성이 다를까?
진짜 connect 타임아웃은 닫힌 포트(즉시 거부)가 아니라 SYN에 무응답인 블랙홀 IP(예: 10.255.255.1:81)로만 재현된다. connect timeout이 없으면 이 연결이 OS 기본값(수십 초)까지 스레드를 잡는다. 학습 테스트에서 가볍게 실험해보자.
