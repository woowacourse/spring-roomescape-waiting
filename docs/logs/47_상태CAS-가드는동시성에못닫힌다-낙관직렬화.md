# 47. 앱 가드는 동시성에 못 닫힌다 — 상태 CAS(낙관)로 직렬화

**날짜**: 2026-06-18
**학습 범위**: log_46 §4 씨앗 — reconcile의 `if status != NEEDS_CHECK return` 가드가 *진짜 동시성*에서 닫히나? → 앱-레벨 가드의 한계 확인 + 상태 전이를 DB CAS로 직렬화. #28 "새치기 가드 직렬화 증명"의 재방문.

분류: 동시성 / 분산 — 코드 적용 (재방문: #28 새치기 가드·직렬화, #06·#08 낙관 락, #03 INSERT 경쟁)

## 0. 계기
reconciliation 워커가 NEEDS_CHECK를 주워 수렴시키는데, 그 사이 사용자가 recheck하면? 둘 다 상태를 바꾸려 한다. "낙관 가드로 멱등"이라 했지만 — 가드가 *진짜 동시*에 닫히는지 미검증이었다(log_46에서 씨앗으로 남김).

## 1. 앱-레벨 가드는 원자적이 아니라 못 닫는다
- `read(status) → if != NEEDS_CHECK return → write(complete)` 는 세 단계라 **원자적이 아니다.** T1이 NEEDS_CHECK 읽고 commit하기 전에 T2도 자기 트랜잭션에서 NEEDS_CHECK를 읽으면 → **둘 다 가드 통과 → 둘 다 complete.**
- `Order.complete`의 "이미 완료" 인메모리 가드도 못 잡는다 — 각자 읽은 status가 stale한 NEEDS_CHECK라서.

## 2. 동시성의 출처 — 워커 수가 아니다
- **worker vs recheck (항상)**: recheck=사용자 HTTP 요청(웹/Tomcat 스레드), worker=`@Scheduled`(스케줄러 스레드). *서로 다른 스레드*라, **워커를 단일로 돌려도 이 충돌은 남는다.** "워커 하나만"으로는 안 풀린다.
- **worker vs worker (분산)**: 앱을 *여러 인스턴스*로 띄우면 각 인스턴스의 스케줄러가 같은 NEEDS_CHECK를 집는다 → 이게 분산 시스템 얘기.
- 학습자 통찰: 멀티스레드/단일 워커로 *통제하려 하지 말 것* — 동시성의 출처가 스레드 수가 아니므로.

## 3. 해법 — 상태 CAS = 낙관 락 (status가 곧 version)
```sql
UPDATE orders SET status='CONFIRMED', payment_key=? WHERE order_id=? AND status='NEEDS_CHECK'
```
- 동시에 둘이 날려도 DB가 row를 원자적으로 처리 → 한쪽만 1행(이김), 다른 쪽은 **0행**(이미 바뀜=짐). 바뀐 행 수로 "내가 이겼는지" 안다.
- 학습자 통찰: **"낙관락을 status로 구현."** version 컬럼은 *아무 필드나* 바뀔 때 필요하고, 우리처럼 *상태 전이가 곧 가드*면 **의미 있는 상태(status)가 version 역할.**
- **핵심 한 줄**: 동시성을 *compute(스레드/인스턴스 수)*로 통제하지 말고, **공유 상태(DB)에서 직렬화하라.** 그러면 스레드 몇 개든·인스턴스 몇 개든 한쪽만 이기고, *미리 잠그지 않으니(낙관)* 경합 없을 땐 공짜. (비관락=SELECT FOR UPDATE로 *막는 것*과 대비.)

## 4. 구현
- `OrderDao.compareAndUpdate(order, expectedStatus)` → `UPDATE ... WHERE status=:expected`, 반환=바뀐 행 수.
- `OrderService.complete/markFailed` → 전이 전 status 기억 → CAS → **boolean(이김/짐)** 반환.
- **이긴 호출만 예약 후속**(`reservation.confirm`/`cancelPending`) 진행. confirm·recheck·reconcile·abandon **네 수렴 지점 전부** 이 게이트로.

## 5. 증명 (과 그 한계)
- 테스트 `compareAndSetSerializesConverge`: 둘 다 NEEDS_CHECK를 *먼저 읽고*(stale-read 흉내) → first.complete=true(1행) / second.complete=false(0행, 이미 CONFIRMED). **메커니즘(0행=짐)을 순차로 증명.**
- **정직한 한계**: H2 + 단일 트랜잭션이라 *진짜 멀티스레드 동시 실행* 증명은 아니다. 그건 **Testcontainers MySQL + 스레드**가 필요(#28에 그대로 적힌 그 항목). 메커니즘은 잠갔고, 진짜 동시성 증명은 다음.

## 6. 한 문장 봉인
> 앱-레벨 가드(read→check→write)는 원자적이 아니라 동시성에 *못 닫힌다.* 닫으려면 **공유 상태(DB)에서 CAS** — 의미 있는 status를 version 삼은 낙관 락. 그러면 스레드·인스턴스 수와 무관하게 한쪽만 이기고(직렬화), 비관락처럼 미리 잠그지 않아 경합 없을 땐 공짜. 동시성은 compute가 아니라 *shared state*에서 잡는다.

## 학습법 회고
- **잘된 것**: "가드가 동시성에 닫히나?"를 *구체 2-스레드 인터리빙*으로 스스로 추론(stale-read→둘 다 통과). 거기서 version 낙관락→**status를 version으로**→분산(여러 인스턴스)→"compute 말고 shared state에서 직렬화"까지 *한 호흡에 인출·연결*. #28을 현재에 꿰는 재방문의 모범. 그리고 **"비관 아니고 낙관이네"**의 깨달음 = 두 락을 *대비*로 구조화.
- **메타(잘된 것 2)**: log_46 "바꿀 것"(토론형엔 퀴즈 대신 *역질문*)이 적용됨 — 학습자가 *"워커 멀티스레드 필요한가?"*, *"핫인데 연관이 안 됐나?"* 로 **코치에게 빈틈을 되물음**. 특히 후자는 *arc 응집 감시*(흩어진 픽 감지) = BACKLOG 운영 규칙을 학습자가 *체화*한 신호.
- **바꿀 것(다음)**: arc 응집을 학습자가 짚었으니, 다음 사이클은 *시작 전에* "이게 지금 타는 실과 같은 갈래인가"를 *한 줄로 명시*하고 들어가기(흩어진 픽 예방). #47(동시성)은 보상 arc와 결이 달랐음을 인정하고 분류 정리.

## 다음 사이클 키워드 (i+1)
- **진짜 멀티스레드 직렬화 증명 — Testcontainers MySQL + 동시 스레드** — 예고 §5·#28 / 종류: 코드 적용 (H2로는 못 보는 진짜 경합에서 CAS가 한쪽만 통과시키나)
- **(arc 전환) 능동적 실패/보상 — #2 재시도 정책 + #3 Saga 보상** — 돈이 움직인 뒤 실패→환불(log_31 개념 재방문). #47(동시성)과는 *다른 갈래*라 arc 분리.
