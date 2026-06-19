미션 배경

지금까지 만든 방탈출 예약 서비스는 정보를 입력하면 곧바로 예약이 확정된다. 실제 서비스라면 그 사이에 결제가 있어야 한다. 이 단계는 새 프로젝트가 아니라 그 위에 외부 결제 서비스 Toss Payments 연동을 이어 붙이는 작업이다. 결제 로직을 직접 구현하지 않고 외부 API를 호출해 처리한다. 핵심은 결제 도메인이 아니라 외부 API(third party API) 연동 경험이다. 테스트(샌드박스) 키만 쓰므로 실제 출금은 없다.

시작 전 학습 테스트 learning-test-1-api-error를 먼저 푼다. 거기서 익힌 패턴(RestClient 호출, onStatus 에러 매핑, 금액 검증, 포트 & 어댑터)을 내 서비스에 적용하는 것이 이 단계다. 이때 서버가 호출하는 유일한 코어 API는 결제 승인 API(POST /v1/payments/confirm)다.

목표
예약 생성 흐름에 결제 단계를 끼워, 결제가 성공해야 예약이 CONFIRMED 되게 한다.
브라우저 Toss 결제창 SDK로 인증받고, 서버는 승인 API(POST /v1/payments/confirm)만 RestClient로 호출한다.
외부 에러 응답({code, message})을 도메인 예외로 변환하고, 금액 위변조를 승인 전에 차단한다.
외부 연동을 포트 & 어댑터(부패 방지 계층) 로 감싸 도메인이 Toss에 결합되지 않게 한다.
요구사항
1. 결제 전 주문 정보 저장
   예약 생성 요청이 들어오면 결제 인증 전에 주문 정보(orderId, 최종 amount)를 먼저 저장한다. orderId는 서버가 생성하며(6~64자, 영숫자/-/_) 이후 금액 검증의 기준이 된다. 이 시점의 예약은 아직 확정이 아니다(결제 대기).

2. 브라우저 결제창 연동 (클라이언트)
   예약 페이지에 Toss 결제창 SDK를 붙여 인증받는다(위젯 초기화는 클라이언트 키 test_ck_). 카드 정보 입력·인증은 결제창과 카드사가 처리하며 서버는 카드번호를 절대 만지지 않는다. 인증이 성공하면 토스가 successUrl로 paymentKey, orderId, amount를 넘긴다.

3. successUrl 콜백 — 금액 검증 후 승인
   콜백으로 넘어온 amount를 그대로 믿지 않고 주문 저장 금액과 대조한다. 다르면 PaymentAmountMismatch류 예외로 승인 호출 전에 차단한다. 토스엔 금액 불일치 전용 코드가 없어 서버가 직접 검증해야 한다.

일치하면 승인 API를 호출한다. 성공하면 이후 조회·취소에 필요한 paymentKey를 DB에 저장하고 예약을 CONFIRMED로 바꾼다.

4. 결제 승인 API 호출 (RestClient)
   POST https://api.tosspayments.com/v1/payments/confirm를 RestClient로 호출한다.

바디 3필드: paymentKey, orderId, amount (Content-Type application/json).
인증은 Basic: base64(시크릿키 + ":")를 Authorization: Basic ...로 보낸다(콜론 뒤 비밀번호는 비우고, 인코딩 시 UTF-8 명시).
시크릿 키(test_sk_)는 노출/하드코딩 금지 — application.yaml 등으로 외부화한다. 시크릿 키는 서버 승인 전용이다(클라이언트 키와 역할이 다름).
5. 관심사 분리 — 포트 & 어댑터
   도메인/애플리케이션 계층에 PaymentGateway 포트와 도메인 모델(PaymentConfirmation, PaymentResult)을 두고, PaymentService는 Toss와 Toss DTO를 몰라야 한다. Toss DTO(요청/응답/에러) ↔ 도메인 모델 번역은 어댑터 TossPaymentGateway(부패 방지 계층, ACL)가 맡는다. PG사를 바꿔도 어댑터만 새로 만들면 되고 도메인은 그대로다.

6. 에러 응답을 도메인 예외로 매핑
   onStatus(HttpStatusCode::isError, 핸들러)로 4xx/5xx를 가로챈다. 핸들러에서 본문을 TossErrorResponse({code, message})로 역직렬화한 뒤 도메인 예외로 변환한다. 변환은 어댑터 안에서 일어나고 Toss DTO는 밖으로 새지 않는다. 변환한 예외는 사용자 응답으로도 의미 있게 이어진다(카드 거절은 안내, 키 오류는 알람 등). code별 분기 방향(자기 서비스에 맞게 설계):

HTTP	code	처리 방향
400	ALREADY_PROCESSED_PAYMENT	이미 승인됨(재시도·새로고침)
400	DUPLICATED_ORDER_ID / NOT_FOUND_PAYMENT_SESSION / INVALID_REQUEST	중복·만료·잘못된 요청
401	UNAUTHORIZED_KEY / INVALID_API_KEY	키 설정 오류 — 운영 알람
403	REJECT_CARD_PAYMENT	카드 거절 — 사용자 안내
404	NOT_FOUND_PAYMENT	결제 건 없음
500	FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING	토스 내부 오류 — 재시도 대상
그 외	미정의	기본 예외
정확한 목록은 Toss Payments 에러 코드의 "결제 승인" 섹션을 참고한다.

7. failUrl(취소/실패) 처리
   failUrl로 code, message, orderId가 넘어온다. 실패 사유를 사용자에게 보여주고 결제 대기 상태의 주문/예약을 정리한다. 단, 사용자가 취소(PAY_PROCESS_CANCELED)하면 orderId가 없을 수 있으니 null 가드를 둔다.

완료 기준
테스트 카드로 결제 인증 → 승인 → 예약 확정(CONFIRMED) 전체 흐름이 동작한다.
조작된 amount는 승인 호출 전에 차단되고 게이트웨이가 호출되지 않는다.
주요 에러코드(이미 처리됨/카드 거절/키 오류/재시도 대상)가 도메인 예외와 사용자 응답으로 처리되고, 미정의 코드는 기본 예외로 떨어진다.
PaymentService가 Toss를 모른다(Toss DTO·에러 매핑이 어댑터 뒤로 격리됨).
시크릿 키가 설정으로 외부화되어 있고, failUrl의 orderId 없는 취소에서도 NPE가 없다.
Q. PR 링크

답변을 입력하세요...
0/5000자

더 생각해보기
같은 paymentKey로 승인을 두 번 호출하면 ALREADY_PROCESSED_PAYMENT(400)가 온다. 이를 "에러"로 볼지 "이미 성공한 결제"로 볼지,
onStatus를 4xx와 5xx로 나눈다면, 5xx(서버 오류) 중 어떤 예외를 재시도해도 안전할까?
브라우저 결제창은 왜 클라이언트 키로, 서버 승인은 왜 시크릿 키로 호출할까?
지금은 외부 호출이 항상 빠르게 응답한다고 가정했다. 토스가 느리거나 연결되지 않으면 우리 서버는 어떻게 될까?
