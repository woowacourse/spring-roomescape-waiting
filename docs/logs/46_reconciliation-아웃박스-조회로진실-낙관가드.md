# 46. reconciliation — 불명확을 조회로 수렴(아웃박스 폴링) + 멱등을 락 아닌 낙관 가드로

**날짜**: 2026-06-18
**학습 범위**: 토스 step2의 cold 백로그 「능동적 실패/보상」 arc 진입 — NEEDS_CHECK(결과 불명확) 주문을 사용자 없이도 자동 수렴시키는 reconciliation 설계·구현. + 미션 "더 생각해보기" #1~5 토론(경계값·connect/read·확인필요·블랙홀 connect timeout).

분류: 외부 결제 연동 / 분산·최종 일관성 — 코드 적용 (재방문: #28 아웃박스·멱등, #06·#08 낙관 락, #26/#28 직렬화)

## 0. 계기
step2에서 read timeout을 "모름"(NEEDS_CHECK)으로 두고 *사용자-주도 recheck*까지 만들었지만, 사용자가 영영 안 오면 NEEDS_CHECK가 림보. 그걸 자동 수렴시키는 게 reconciliation(백로그로 남겼던 것).

## 1. 조회 = 진실의 원천 → connect/read 구분이 녹는다
- 핵심 통찰(학습자): **토스에 조회하면 NEEDS_CHECK가 둘로 갈린다** — DONE(APPROVED) / 아니면(NOT_APPROVED). "모름"이 진실 앞에서 이진으로 수렴.
- 왜 항상 답이 나오나: **우리한테 paymentKey가 있다 = 토스가 위젯 단계에서 그 결제를 *발급*했다 = 토스가 안다.** connect 실패였어도(confirm만 안 닿음) 위젯이 이미 결제를 만들었으니 토스는 READY로 갖고 있음 → 조회하면 답.
- 그래서 reconciliation 입장에선 **connect/read 구분이 무의미.** (step2의 *멱등 재시도*가 양쪽을 안전하게 만든 것과 **쌍둥이 통찰** — 재시도든 조회든, 토스에 물으면 구분이 녹는다.)

## 2. 아웃박스/폴링 패턴 재인식 (= 나선형의 재미)
- 학습자: *"아웃박스 패턴으로 계속 풀어나가는 게 재밌어. 처음에 고민하던 걸 계속 적용하고 있어."*
- NEEDS_CHECK 주문들 = **처리 대기 큐**. `@Scheduled` 워커가 주워 토스에 묻고 수렴 → `PromotionOutboxWorker`(#28)·`ExpiredOrderWorker`(reaper)와 **같은 폴링 패턴 가족**(최종 일관성). 워커는 얇게 폴링+위임, 로직은 서비스 소유, 건별 격리.
- 같은 토대(폴링·멱등·최종일관성)를 *다른 깊이*에서 다시 만나는 것 = 나선형 학습의 핵심.

## 3. 수렴 = 새 로직 아닌 *조회 + 기존 재사용*
- DONE → "주문 확정 + 예약 BOOKED" = 기존 confirm-성공 ops / not-DONE → "주문 FAILED + 예약 취소" = 기존 abandon ops.
- **진짜 새로 짠 건 조회(`PaymentGateway.findStatus`) 하나.** 토스 `GET /v1/payments/orders/{orderId}` → status를 도메인 `PaymentApprovalStatus`로 번역(ACL — 토스 포맷 안 샘, confirm이 PaymentResult 돌려준 것과 동형). 수렴 조율은 `PaymentReconciliationService`.

## 4. 멱등: 락(과거) → 낙관적 상태 가드(이번)
- 학습자: *"멱등성을 저번엔 락을 썼다면, 지금은 낙관적으로 처리하면서 가드를 썼잖아."*
- `reconcile`: 워커가 orderId를 *주워온 순간*과 *처리하는 순간* 사이 시차 → 사용자가 recheck로 이미 수렴시켰을 수 있음. 그래서 **막지(락) 않고**, 처리 시점에 `status != NEEDS_CHECK면 return`(현재 상태 재확인 후 건너뜀). `abandon`의 `!isPending()`, `getOrCreate`와 같은 멱등 규율.
- **씨앗(다음 키워드)**: 가드는 락이 아니라 *체크*라, 워커와 recheck가 *진짜 동시* 실행되면 둘 다 가드 통과 가능(`Order.complete`의 "이미 완료" 가드가 백스톱). "가드만으로 진짜 동시성이 닫히나?"는 #28 *"새치기 가드 직렬화 증명"*과 같은 모양 — 미해결 재방문.

## 5. "더 생각해보기" #1~5 토론
- **#1 경계값**: read timeout ≈ 평균 응답이면 변동성 때문에 *반반 들쭉날쭉*(race). 값은 평균X·최댓값X → **분포의 높은 백분위수(p99)+여유**. 너무 높음=스레드 점유 / 너무 낮음=healthy를 *우리가* "모름"으로 만들어 재시도·멱등 의존 폭증.
- **#2 connect vs read**: connect는 처리 없어 즉시 → *더 짧게* OK(죽은 연결 빨리 포기), read는 토스 승인 처리시간 필요 → 더 길게. + connect 실패="확실히 안 됨"(빨리 끊어도 안전), read="모름"(성급히 끊으면 위험).
- **#3 확인필요 vs 실패**: 실패 단정하면 *결제됐는데 예약 취소*(돈↔상태 불일치). 확인필요로 두면 수렴 가능 — 수렴 장치 = recheck(만듦) + reconciliation(이번).
- **#4 재시도 안전성**: connect=요청 도달조차 안 함 → 재시도 무방 / read=됐을 수도 → 멱등키 필요.
- **#5 블랙홀 connect timeout**: 닫힌 포트=즉시 RST(ConnectException, 타임아웃 아님) / 블랙홀=SYN 무응답 → connect timeout(없으면 OS 기본값 수십 초 스레드 점유). 반전: 블랙홀은 root cause가 `SocketTimeoutException`이라 **우리 분류에선 "확실히 안 됨"이 아니라 "모름"** (ConnectException만 확실히 안 됨; 안전 기본값). 복구(멱등 재시도·조회)가 양쪽 안전하니 무방.

## 6. 한 문장 봉인
> 불명확(NEEDS_CHECK)은 토스에 *조회*하면 둘로 갈린다 — 조회가 진실의 원천이라 connect/read 구분이 녹는다(멱등 재시도와 쌍둥이). 그 수렴을 *아웃박스 폴링*(reaper·#28과 같은 패턴)으로 돌리고, 새 로직 없이 기존 confirm/abandon을 재사용하며, 멱등은 *락이 아니라 낙관적 상태 가드*로 지킨다.

## 학습법 회고
- **잘된 것**: 설계를 *전부 인출/co-design*한 뒤 구현(조회 키·수렴 방향·포트 위치·아웃박스 인식·가드 이유까지 학습자가 끌어냄). 특히 #1~5 토론에서 *스스로* 경계값 race·connect/read·블랙홀을 추론. 그리고 **"재미"에 도달**(파편→구조) — `user-learning-goal` 그 자체. 락→낙관가드 연결은 *과거 사이클을 현재에 꿰는* 나선형 인출의 모범.
- **바꿀 것(다음 타임)**: 이번은 한 arc(reconciliation) 집중이라 #45의 "phase 경계 한 줄 캡처"가 자연히 지켜짐(토론마다 한 줄씩 봉인). 유지. 새 엣지: *재방문 씨앗을 그 자리에서 BACKLOG가 아닌 로그 본문에 적기*는 잘 됨 — 다음엔 토론형 사이클에서 **퀴즈 대신 "역질문"(학습자가 코치에게 빈틈을 묻게)** 한 번 시도.

## 다음 사이클 키워드 (i+1)
- **reconcile 가드 vs 락 — 진짜 동시(worker+recheck)에서 직렬화되나** — 예고 §4 / 종류: 코드 적용 (#28 "새치기 가드 직렬화 증명" 재방문, Testcontainers MySQL 필요할 수도).
- **재시도 정책 — 워커가 영구 재시도(토스 404 등) → 몇 번·언제 포기·백오프** — 예고 §1·log_36 / 종류: 흐름 파악→코드 적용.
- **트랜잭션 밖 외부호출 분리 + Saga 보상** — confirm/reconcile이 @Transactional 안 토스 호출 → 승인 후 후속 실패 시 돈↔상태 불일치 / 종류: 코드 적용.
