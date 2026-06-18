# 44. 타임아웃 방어 — connect/read 두 단계, read=모름, 멱등 두 겹(재방문)

**날짜**: 2026-06-18
**학습 범위**: 토스 결제 step2 시작 — (1) RestClient에 connect/read 타임아웃 설정(req1 코드 적용), (2) 타임아웃이 어떤 예외로 표면화되고 토스 에러와 어떻게 구분되는지(req2 개념), (3) read timeout="모름"이 #36 멱등성을 *왜* 부르는지 재방문·심화. 코드는 req1까지 적용, req2~4는 다음 사이클.

분류: 외부 결제 연동 / 네트워크 — 키워드 이해→코드 적용 (신규: 타임아웃 2단계·요청 팩토리 / 재방문: #36 멱등성, #39 설정 외부화)

## 0. 계기
step1의 토스 confirm 호출엔 타임아웃 방어가 없다. 토스가 느리면 그 한 번이 우리 스레드를 무한정 붙잡아 풀 고갈 → 결제 무관 요청까지 멈춤. "느린 호출을 일찍 포기"가 출발점.

## 1. 타임아웃은 두 단계 — HTTP 호출엔 "기다리는 순간"이 두 번
전화 비유로 도출: ① 전화를 건다 → 상대가 *받기까지* 기다림 = **connect timeout**(연결 맺기). ② 받았다 → 상대가 *대답하기까지* 기다림 = **read timeout**(응답 읽기). 연결이 맺어지는 순간 connect는 임무 끝, 거기서 read가 바통을 받는다.

## 2. read timeout = 실패가 아니라 "모름" (핵심)
- connect 실패 = 전화가 아예 안 걸림 → 토스가 요청을 **못 받음** → 결제 **확실히 안 일어남**.
- read 실패 = 전화 걸려서 "승인해줘"까지 말했는데 응답이 안 옴 → 토스는 **이미 승인했을 수도** 있고 응답만 유실됐을 수도 → **모름**.
- 그래서 read timeout을 "결제 실패"로 단정하면 안 된다. 토스 입장에선 진행/완료 중인데 우리가 응답을 못 받아 끊은 것뿐.
- 저울: read-timeout을 짧게 → 스레드 빨리 회수(좋음) ↔ *원래 성공할 호출까지 끊겨* "모름" 케이스·재확인 요청 증가(대가). 짧을수록 멱등키가 **더** 중요해진다.

## 3. confirm 결과 지도 — 셋을 한 덩어리로 뭉개지 않기 (req2 개념)
| 갈래 | 표면화 예외 | 의미 | 재시도 안전성 |
|---|---|---|---|
| 토스 에러 응답 `{code,message}` | `onStatus`→`TossPaymentException` | 거절 | (사유 따라) |
| connect 실패 | `ResourceAccessException` (root: ConnectException/SocketTimeoutException) | 확실히 안 됨 | 그냥 재시도 안전 |
| read 실패 | `RestClientException` (root: SocketTimeoutException) | 모름 | **멱등키 있어야** 안전 |
- `onStatus`는 *도착한 응답의 상태*를 검사하는 핸들러 → 응답 자체가 없는 timeout은 안 걸리고 호출 지점에서 예외로 빠져나간다(따로 catch 필요).
- 표면화 예외의 실제 타입은 learning-test(MockWebServer + 경과시간 단언)로 *몸으로* 확인하는 게 미션 의도.

## 4. req1 코드 — 타임아웃은 저수준 factory에 (왜 builder가 아닌가)
- 층위: `RestClient`(고수준: "이 URL로 POST" 의도) → `ClientHttpRequestFactory`(연결 객체 공장) → `ClientHttpRequest`(실제 소켓·TCP). 타임아웃은 "소켓을 몇 초 기다릴까" = **저수준 연결 설정** → builder엔 `.connectTimeout()`이 없고 factory가 들고 있다.
- 적용(코드 방식 B): `SimpleClientHttpRequestFactory` 만들어 `setConnectTimeout/setReadTimeout` → `builder.requestFactory(factory)`. 값은 `TossProperties`(Duration)로 외부화, yml `connect-timeout: 2s / read-timeout: 5s`.
- A(프로퍼티 `spring.http.client.*`, 단수형이 3.4) vs B(코드) 갈림길: A는 **모든** RestClient에 전역 적용, B는 **토스 전용 국소**. 우리는 토스에 맞춘 값 + 설정 한곳(TossProperties)에 모으려고 B. (미션이 든 복수형 `spring.http.clients.*`은 Spring Boot 4.0 전용 → 이 프로젝트(3.4.4)엔 없음 — 추측을 build.gradle로 검증해 확인.)
- 🔖 파킹된 의문: 왜 jdk 팩토리는 응답 바디 지연을 read timeout으로 못 잡나 → 다음 사이클.

## 5. 멱등 재방문(#36) — 키가 두 개, 두 겹의 방어
- read timeout="모름" → 재시도해야 하는데 confirm은 POST(돈 움직임) → 무작정 재시도 = 이중 승인. 막는 게 멱등.
- **키가 두 개**(퀴즈에서 처음엔 conflate, volley로 갈라냄):
  - `paymentKey` — *토스가* 발급. 이미 처리된 결제를 토스가 알아채고 `ALREADY_PROCESSED_PAYMENT` **에러로 거절** = step1의 층.
  - `Idempotency-Key` — *우리가* 만들어 confirm 헤더로 보냄. 같은 키 = 토스가 "전에 본 요청"으로 보고 **처음 응답을 그대로 반환** = step2(오늘)의 층.
- 행동 차이가 핵심: 같은 멱등키 → *깔끔한 성공 재현* / 다른(매번 새) 멱등키 → 멱등층 무력화 → paymentKey 백스톱이 *에러*로 막음(이중승인은 안 나지만 지저분). 그래서 "키는 주문 고정"은 단순 규칙이 아니라 **재시도를 성공으로 만들지 에러로 만들지**를 가른다.
- 키를 *우리가* 발급해야 하는 이유(#36 결론 재확인): 토스가 응답에 키를 담아줘도 read timeout으로 그 응답이 유실되면 키도 사라짐 → 재시도 때 같은 키를 못 씀. 우리가 요청 전에 만들어야 유실에도 살아남는다.

## 6. 한 문장 봉인
> 타임아웃은 두 단계(connect=받기까지, read=대답까지)고, **read 실패는 '실패'가 아니라 '모름'** — 짧게 잡을수록 모름·재시도가 늘어 멱등키가 더 중요해진다. 타임아웃은 의도(builder)가 아니라 *연결을 만드는 저수준 factory*에 건다. 멱등은 두 키 두 겹: 우리 발급 Idempotency-Key(같은 키→성공 재현) + 토스 paymentKey(ALREADY_PROCESSED 백스톱).

## 7. req2 실행에서 발견 (deepening, 후속 — 코드로 옮기며 비자명하게 알게 된 것)
- **factory 선택은 공짜가 아니다**: req1의 `simple`(HttpURLConnection)이 **401 응답 바디를 인증 처리 중 삼켜** 토스 에러코드 매핑(UNAUTHORIZED_KEY)이 깨짐(기존 테스트가 잡음). jdk는 read 바디 지연 못 잡고 simple은 401 본문 삼킴 → **apache(HttpComponents)**로 전환(httpclient5 +). "simple 또는 apache" 중 simple의 함정을 *코딩 중* 발견.
- **미션 표를 측정으로 반증**: 미션은 read→`RestClientException`이라 했지만, MockWebServer 느린응답 테스트로 보니 **read timeout도 `ResourceAccessException`으로 표면화**(Spring이 SocketTimeout을 그렇게 쌈). → wrapper 타입 분류는 read를 connect로 **오분류**(가장 위험한 방향). **root cause로 전환**: `ConnectException`(연결 거부)만 "확실히 안 됨", 그 외는 **안전 기본값 "모름"**. 교훈: *스펙(미션 표)을 믿지 말고 학습-테스트로 측정하라* — 미션이 learning-test를 시킨 이유가 이거.
- **@Transactional 롤백 함정의 해결**: read="모름"일 때 주문을 `NEEDS_CHECK`로 UPDATE해도 예외를 *다시 던지면* 롤백돼 마크가 사라짐. 해결: **"확인 필요"를 에러가 아니라 *결과(ConfirmOutcome)*로 모델링** → 예외를 catch하고 *안 던지니* 트랜잭션이 마크를 커밋(REQUIRES_NEW·checked보다 결이 맞음). 테스트가 이 커밋까지 검증(마크 안 살면 PENDING이라 실패).
- **reaper 제외는 공짜**: NEEDS_CHECK 보호 전제(reaper가 안 건드림)는 `findExpiredPending`이 `status='PENDING'`만 줍어 *이미* 성립 — 쿼리로 확인.
- **씨앗(cold)**: 사용자가 안 돌아오면 NEEDS_CHECK 림보 → 자동 reconciliation(토스 결제상태 조회) 필요. 이번 미션은 *사용자-주도* 재시도로 갈음(Saga/보상 영역이라 범위 밖).

## 학습법 회고
- **잘된 것(엣지 유지)**: log_43 "바꿀 것"(코드 전 개념을 *입으로 인출* + 재방문은 "전에 어디서?"부터 volley-back)을 그대로 적용 — req1 코드 전에 척추 전체를 학습자가 인출, 멱등성도 "어디서 했지?"→#36 인출부터. 추측을 사실로 검증(스프링부트 버전을 build.gradle로 확인)하는 무브도 유지.
- **학습자 자기교정(메타)**: 코치가 "적용률 높음=컴포트존, 새 엣지" 하려 하자 학습자가 *"1회 적용으로 단정 말자. 변화가 아니라 쌓는 거니 하나씩"*으로 바로잡음. → **컴포트존 판단은 1회로 내리지 않는다. 학습자가 '쌓는 중'이라 하면 같은 엣지를 반복하게 둔다.**
- **바꿀 것(다음 타임)**: 새 엣지 추가는 **보류**(쌓기 우선). log_43 엣지를 req2~4 코드에서 의식적으로 *반복*해 체득 확정. 단, 퀴즈에서 드러난 것 — 재방문 개념(멱등 두 겹)은 "흐름 인출"은 되는데 *quiz 압박에서 "어느 층이 무슨 행동을 하나" 정밀도가 무너짐* → 재방문 주제는 quiz를 "시나리오 적용"(새로고침/새 UUID 같은 구체 케이스)으로 더 일찍 던지면 conflate가 빨리 드러난다.

## 다음 사이클 키워드 (i+1)
- **req2 — 타임아웃 예외 3갈래 코드 적용**: `ResourceAccessException`(connect)/`RestClientException`(read)을 catch해 도메인 의미로 번역, read는 "결제 실패" 아닌 **"확인 필요"** 상태로. 어디서 잡나(gateway ACL vs service)도 설계. 종류: 코드 적용.
- **req3 — Idempotency-Key 코드**: 주문당 고정 UUID 생성·저장(orderId 직접 vs 전용 컬럼 결정) + confirm 헤더 전송. 개념은 닫힘, 코드만. 종류: 코드 적용.
- **req4 — 주문/결제 내역 페이지**: 예약정보 + 결제상태(대기/확정/**확인 필요**)·orderId·paymentKey·금액. req2의 "확인 필요" 상태가 여기서 표면화. 종류: 코드 적용.
- **(파킹) 왜 jdk 요청 팩토리는 응답 바디 지연을 read timeout으로 못 잡나** — 종류: 흐름 파악.
