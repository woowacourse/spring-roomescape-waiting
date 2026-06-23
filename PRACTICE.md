# 결제 기능 직접 구현 — 학습 가이드

`step3-practice` 브랜치. 베이스 `21a5020`(결제 직전)에서 시작한다.
**`step3` 브랜치가 정답지**다. 직접 구현하다 진짜 막힐 때만 본다.

개발 순서: **흐름도 → 설계(순서·책임) → 슬라이스 하나씩.** 흐름이 프론트·백을 오가므로, 바텀업으로 계층을 다 쌓기 전에 흐름을 먼저 잡는다.

## 규칙

- 흐름(토스 프로토콜)은 **읽어서 아는 것**(외부 사실). 설계(내 코드 책임 분배)와 구현은 **직접** 한다.
- 테스트(spec)만 슬라이스마다 `step3`에서 끌어온다. production 코드는 직접 짠다.
- 정답 보기:
  - 파일 읽기(브랜치 안 바꾸고): `git show step3:<경로>`
  - 다 끝낸 뒤 내 구현과 비교: `git diff step3 -- <경로>`
  - **읽었으면 닫고, 이해한 걸로 다시 타이핑한다. 복붙 금지.**
- Java는 테스트 소스셋 전체가 컴파일돼야 돈다. 그래서 그 슬라이스 테스트만 끌어온다(다음 슬라이스 테스트를 미리 넣으면 컴파일이 깨진다).

## 요구사항에서 뽑은 핵심 사실

- 예약은 `PENDING` → 결제 승인 시 `CONFIRMED`.
- 주문 금액은 **50,000원 고정**.
- 결제 승인은 Toss에 위임하되, **금액 검증은 우리 서버가 승인 호출 전에** 한다(조작 금액 차단).
- 결제 실패/취소 시 `PENDING` 예약과 주문을 정리한다. `orderId`가 없어도 정상 응답.
- 클라이언트는 SDK(`requestPayment`, clientKey), 서버는 REST(`/v1/payments/confirm`, secretKey).

---

# Stage 0 — 구현 전 준비 (흐름도)

## 0a. 자료 읽고 흐름도 그리기  ← 가장 먼저

코드 짜기 전에, **누가(클라이언트/서버) 무슨 호출을 어떤 순서로** 하는지 한 장에 그린다. 이건 발명이 아니라 독해다.

- **입력 자료:**
  - 사전학습 `LMS+1.pdf` — "결제 위젯 연동 4단계"(렌더링→요청→인증→승인)
  - 토스 [결제 흐름 이해하기](https://docs.tosspayments.com/guides/v2/get-started/payment-flow), [결제위젯 연동하기](https://docs.tosspayments.com/guides/v2/payment-widget/integration), [JS SDK 레퍼런스](https://docs.tosspayments.com/sdk/v2/js#widgetsrequestpayment)
- **산출물:** 시퀀스 흐름도 한 장 (사용자 / 클라이언트 / 서버 / 토스).
- **DoD:** 아래에 스스로 답할 수 있다.
  - [ ] 클라이언트가 토스에 보내는 건? 서버가 토스에 보내는 건? (둘을 구분)
  - [ ] `successUrl`로 돌아오는 값 3개는? 그걸 누가 받아서 어디로 넘기나?
  - [ ] clientKey와 secretKey는 각각 어디서 쓰나?
  - [ ] 실패/취소는 어디로(어떤 URL) 돌아오고 무엇을 정리하나?

---

# 개발 — 슬라이스 하나씩

각 슬라이스: spec 테스트 켜기 → red → 구현 → green → 책임 점검 → 커밋.
슬라이스 안에서 BE는 잠깐 도메인→레포→서비스→컨트롤러로 쌓인다(슬라이스 하나로 좁혀진 바텀업). FE는 테스트가 없어 수동 확인.

## 슬라이스 1 — 예약하면 결제 대기 주문이 생긴다

- **흐름:** 예약 등록 → `POST /reservations` → 서버가 예약 PENDING + 주문 발급 → `orderId`·`amount` 반환
- **spec 켜기:**
  ```
  git checkout step3 -- src/test/java/roomescape/integration/PaymentJdbcRepositoryTest.java
  git checkout step3 -- src/test/java/roomescape/e2e/ReservationApiTest.java
  ```
- **BE:** 예약 상태(PENDING/CONFIRMED), `Payment` 도메인·`payment` 테이블(schema.sql)·레포, 예약 생성 시 주문 발급, 생성 응답에 `orderId`/`amount`.
- **FE:** 예약 등록 호출은 베이스에 이미 있음. 응답에 주문이 실려 오는지만 확인.
- **DoD:** `PaymentJdbcRepositoryTest` + `ReservationApiTest`(PENDING·주문 단언) green.

## 슬라이스 2 — 결제창(위젯)이 뜬다

- **흐름:** `orderId`·`amount`로 `requestPayment()` → 토스 결제창
- **테스트:** 없음(FE) → 수동
- **FE:** `reservation.html`에 SDK script + 모달, `reservation.js`에서 `widgets.setAmount`/`renderPaymentMethods`/`renderAgreement`/`requestPayment`.
- **DoD(수동):** 예약 등록 → 결제창(모달)이 뜨고 결제수단이 보인다.
- 팁: FE는 학습 핵심이 아니면 `git show step3:...`로 참고해 빠르게 끝내도 된다.

## 슬라이스 3 — 결제하면 예약이 확정된다

- **흐름:** 인증 성공 → `successUrl`(`paymentKey`,`orderId`,`amount`) → `POST /payments/confirm` → 금액 검증 → 토스 승인 → 예약 `CONFIRMED`
- **spec 켜기:**
  ```
  git checkout step3 -- src/test/java/roomescape/e2e/PaymentApiTest.java
  ```
  (`ProblemType.PAYMENT_AMOUNT_MISMATCH` 참조 → 그 타입이 없으면 컴파일부터 막힌다. 같이 만든다.)
  - 선택: 어댑터 에러 매핑 안전망 →
    `git checkout step3 -- src/test/java/roomescape/payment/toss/TossPaymentGatewayTest.java`
    (⚠️ Toss 승인 happy-path는 실제 paymentKey가 필요해 e2e로 안 잡힌다. 이 단위 테스트가 유일한 자동 안전망.)
- **BE:** confirm 엔드포인트, 금액 검증(불일치 422 + `PAYMENT_AMOUNT_MISMATCH`), 승인 포트 + Toss 어댑터(RestClient), 예외→응답 매핑, 예약 `CONFIRMED` 전이.
- **FE:** `payment/success.html` + `payment-success.js`(successUrl 콜백 → confirm 호출).
- **DoD:** `PaymentApiTest`의 `조작된_금액..._422` green + 수동 결제 성공 시 예약 CONFIRMED.

## 슬라이스 4 — 결제 실패/취소하면 정리된다

- **흐름:** `failUrl`(`code`,`message`,`orderId`) 또는 모달 닫기 → `POST /payments/fail` → `PENDING` 예약·주문 정리 (`orderId` 없어도 200)
- **spec:** 슬라이스 3에서 켠 `PaymentApiTest`의 나머지 2개가 대상.
- **BE:** fail 엔드포인트 + 대기 주문 정리(`orderId` null/blank 안전).
- **FE:** `payment/fail.html` + `payment-fail.js`, 모달 닫기 시 정리 요청.
- **DoD:** `PaymentApiTest` 3개 전부 green + 수동 취소 시 PENDING 예약·주문 정리.

---

## 진행 체크

- [ ] 0a 흐름도 작성
- [ ] 슬라이스 1 — 예약 → 주문 (`ReservationApiTest`, `PaymentJdbcRepositoryTest`)
- [ ] 슬라이스 2 — 결제창 (수동)
- [ ] 슬라이스 3 — 승인 (`PaymentApiTest` 금액 / `TossPaymentGatewayTest` 선택)
- [ ] 슬라이스 4 — 실패·취소 정리 (`PaymentApiTest` 나머지)
