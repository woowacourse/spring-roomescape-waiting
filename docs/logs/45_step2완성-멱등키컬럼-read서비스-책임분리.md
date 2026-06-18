# 45. 토스 step2 완성 — 멱등키 전용컬럼·read 서비스 분리·책임 분리(코드 적용)

**날짜**: 2026-06-18
**학습 범위**: 토스 step2 마무리 — req3(Idempotency-Key 코드), req4(주문/결제 내역 조회), 결제 진입점 구멍 메우기(결제하기 + NEEDS_CHECK 재확인), TossGateway·PaymentService 리팩토링. log_44(타임아웃 개념+req1+req2)의 *코드 적용/완성* 단계. 개념은 닫혀 있고 **설계 결정**이 핵심.

분류: 외부 결제 연동 / 아키텍처 — 코드 적용 (재방문: #36 멱등성, #40·#42 패키지·책임)

## 0. 계기
log_44에서 타임아웃 방어(req1·req2)를 세운 뒤, 남은 req3·req4를 코드로 옮기고 "여러 책임이 뭉친" TossGateway·PaymentService를 정리하는 단계. 커밋은 요구사항별로 분리(c1bb69c2 req1 → 605c7938 req2 → 6692383f req3 → c4e30188 req4 → b873eabe 진입점 → 2a5a54df 리팩토링).

## 1. req3 — 멱등키는 orderId 재사용 말고 전용 컬럼
- **역할이 다르다**: `orderId`=주문 정체성(식별자), `Idempotency-Key`=요청 멱등성을 위한 키. 토스에서도 다른 개념(orderId는 ALREADY_PROCESSED 기준, 멱등키는 요청 재현). 지금은 혼용 가능해도 **변경에 불리** → SRP로 분리(전용 `idempotency_key` 컬럼).
- 키는 `OrderService.create`에서 주문 생성 시 **1번** 발급해 고정(confirm마다 X — 매번 새 키면 멱등 무의미, #36 함정). gateway가 `Idempotency-Key` 헤더로 전송, 테스트로 헤더 전송까지 검증.

## 2. req4 — 조회는 별도 read 서비스로 (PaymentService를 안 키운다)
- 내역 조회를 PaymentService에 얹지 않고 **읽기 전용 `PaymentHistoryService`**로 분리. 이유: ① PaymentService를 더 복잡하게 안 만든다(이미 무겁다) ② **조회(read)와 결제진행(write)은 관심사가 다르다**. (reservation·order 서비스만 조합 — 둘 다 이미 payment 의존이라 새 엣지 없음.)
- 예약↔주문 분리(log_42) 유지: 백엔드는 `reservationId`로 주문만 돌려주고, **클라이언트가 예약 목록과 머지**(기존 reservations+waitings 머지 패턴 재사용).

## 3. 진입점 구멍 — 사용자가 요구사항 빈틈을 포착
- req4 후 발견: PENDING("결제 대기")·NEEDS_CHECK("확인 필요") 예약에 **실제로 결제/재확인할 버튼이 없었다.** "결제 대기에서 결제로 넘어가는 진입점이 없어"를 *학습자가 직접* 짚음 → 기능이 end-to-end로 안 굴러가던 빈틈.
- **PENDING → "결제하기"**: 기존 checkout 흐름 재사용(sessionStorage + /payment/checkout → /payments/ready getOrCreate).
- **NEEDS_CHECK → "결제 확인" (B 선택)**: read timeout 때 시도한 **paymentKey를 저장**(`markNeedsCheck(paymentKey)`)해 두고, 버튼은 위젯 없이 **서버가 저장된 키+멱등키로 재confirm**(`POST /payments/recheck`). 토스가 첫 응답을 그대로 돌려줘(멱등) 이중 승인 없이 확정/유지.
  - A(위젯 재결제)를 버린 이유: *"됐는데 또 결제"* — 이미 승인됐을 수도 있는데 새 결제(새 paymentKey/승인)를 만들어 유휴 authorization·어색한 UX. B는 "다시 **confirm**"이라 미션 의도에 부합.
- confirm·recheck가 공유하는 `approve(order, paymentKey)` 추출 — 승인-결과-전파 로직을 한곳에.

## 4. 리팩토링 — 두 관심사를 가른다
- **PaymentService**: [활성 결제 흐름 prepare·confirm·recheck] + [방치 주문 정리 fail·expire·abandon]이 섞여 있었다 → 정리 책임을 **`OrderAbandonmentService`**로 분리(후속 리네임 → **`PaymentAbandonmentService`** + 서비스 3개를 `payment/service`로 이동, d7d519af. "이상한데" 감각 → *배치는 맞고 이름이 문제* — 근본 사건은 '결제 미완료', order·reservation 단방향이라 조율은 코디네이터(payment)에). PaymentService는 승인 흐름만, failUrl 콜백·만료 reaper는 새 서비스에 위임(동작 불변, 253 그린).
- **TossPaymentGateway.confirm()**: 한 메서드에 요청 조립·**에러응답 번역**·**전송예외 번역**·매핑이 섞여 있었다 → `translateErrorStatus`(onStatus)·`translateTransportFailure`로 추출해 confirm은 뼈대만.

## 5. 한 문장 봉인
> 같은 값이라도 *역할*이 다르면 나눈다(orderId vs 멱등키). 조회는 흐름에 얹지 말고 *읽기 전용*으로 뗀다. 불명확(NEEDS_CHECK)의 재시도는 *재결제*가 아니라 *재confirm*(저장 키+멱등키)이다. 한 클래스가 두 일을 하면(활성 흐름 vs 방치 정리 / 요청 vs 에러·전송 번역) 가른다.

## 학습법 회고
- **잘된 것**: log_44 "바꿀 것"(코드 전 입으로 인출/co-design) 또 적용 — req3 키 위치, req4 read 분리, 진입점 A/B, 리팩토링 분해를 *결정 전에* 말로 인출. **특히 학습자가 진입점 구멍을 스스로 포착**(요구사항/독자 관점 환기가 손에 붙음 — feedback 적용). 커밋도 요구사항=경계로 깔끔 분리(리뷰어 관점).
- **바꿀 것(다음 타임)**: 이번 세션이 *너무 길었다*(req2~4+진입점+리팩토링을 한 호흡). 회고를 **끝에 몰아서** 하니 인출 과부하 — feedback「분야 전환 시 끊기」가 정확히 이 상황. 다음엔 **각 커밋/phase 경계에서 "한 줄 원리"를 즉석 캡처**하고 넘어가기(끝의 대형 회고 대신). 인출 게이트는 #43~45 3사이클 연속 적용돼 *체득됨* — 이건 유지.

## 다음 사이클 키워드 (i+1)
- **불명확(NEEDS_CHECK) 자동 reconciliation** — 사용자가 안 돌아오면 림보 → 백그라운드 잡이 토스 결제상태 조회로 확정/실패(이번은 사용자-주도로 갈음). cold「능동적 실패」arc. 종류: 흐름 파악→코드 적용.
- **트랜잭션 밖 외부호출 분리 + Saga 보상** — confirm()이 `@Transactional` 안 토스 호출(승인 후 후속 실패 시 돈↔상태 불일치). 종류: 코드 적용.
- **(파킹) jdk 팩토리가 응답 바디 지연을 read timeout으로 못 잡는 이유** — log_44 파킹 유지. 종류: 흐름 파악.
