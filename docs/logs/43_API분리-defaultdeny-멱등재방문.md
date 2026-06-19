# 43. 예약↔결제 API 분리 + default-deny 인증 + 중복 주문 3중 방어(멱등 재방문)

**날짜**: 2026-06-18
**학습 범위**: 토스 결제 step1 — (1) 예약 생성과 결제 준비를 두 API로 분리(주문을 "결제 시작" 시점에 생성), (2) 인증을 default-deny로 뒤집기(fail-open→fail-safe), (3) 결제창 새로고침이 만든 중복 주문 회귀를 3중으로 막기. #42(사이클 끊기)의 코드 적용 연장 + #36 confirm 멱등성·#30/#31 unique 백스톱의 **재방문·심화**.

분류: 외부 결제 연동 / 아키텍처 — 코드 적용 (재방문: #36 멱등성, #30·#31 unique race·백스톱, #38 ACL, #42 사이클·책임)

## 0. 계기
#42에서 payment↔reservation 사이클을 끊자, `PaymentReadyResponse`가 reservation.web에 어색하게 남는 위화감. 루트는 "`POST /reservations` 하나가 예약 생성 + 주문 생성 + 결제준비 반환을 다 함"이었다(Q1).

## 1. Q1 — 책임을 *시점*으로 가르다
- `POST /reservations` = 예약(PENDING)만. `POST /payments/ready` = 주문 생성 + 준비정보 반환.
- 핵심 통찰: **주문 = "결제 의사"** → 주문은 *결제를 시작할 때* 생기는 게 더 정확. 그러면 결제 미시작 PENDING은 주문이 없고 reaper가 created_at으로 정리 → "Order 없는 PENDING"의 의미가 *승격 대기 + 결제 미시작*으로 **통일**됨.
- PaymentReadyResponse가 payment.web으로 자연 복귀(payment→reservation 단방향이라 사이클 안 생김).

## 2. 인증 default-deny — fail-open ↔ fail-safe
- 증상: 새 보호 엔드포인트(`/payments/ready`)를 AuthFilter 목록에 *빠뜨리면 무인증 노출*. e2e 401로 잡혀서 발견.
- 전환: "보호 경로 나열" → "전 경로(`/*`) + 공용만 통과(shouldNotFilter)" = default-deny + 공용 allowlist.
- 한 줄: **빠뜨렸을 때 어느 쪽으로 실패하나** — 나열식은 fail-open(뚫림, 안 보임), default-deny는 fail-safe(막힘, 바로 보임). 보안 기본값은 *안전한 실패* 쪽으로.

## 3. 중복 주문 3중 방어 — 멱등 재방문
회귀: Q1로 주문이 `/payments/ready`에서 생기니, 결제창 새로고침 = 한 예약에 PENDING 주문 중복 생성. (이전엔 예약 생성 때 1번이라 불가능했던 게 분리로 새로 열림.)
- (a) **getOrCreate** — 살아있는 PENDING 주문 있으면 재사용(앱 멱등).
- (b) **cancelPending 멱등화** — PENDING 아니면 no-op → reaper가 BOOKED 예약에서 예외 안 던짐(무한 재시도 차단). 주석으로 표방하던 "멱등"을 *실제로*.
- (c) **UNIQUE(reservation_id)** — 동시 요청 경합까지 막는 DB 백스톱 + DuplicateKey catch→재사용.
- 왜 3겹? 단일 트랜잭션 가정(a) → 잔여 race(c) → 그래도 새는 stale(b). **한 겹으로 안 되니 겹쳐 막는다.**

## 4. 한 문장 봉인
> ① 책임은 *시점*으로도 가른다(주문=결제 시작). ② 빠뜨렸을 때 *안전한 쪽으로 실패*하게 기본값을 잡는다(default-deny). ③ 멱등은 한 겹이 아니라 앱·정리·DB로 겹쳐 막는다 — unique가 #30·#31에 이어 또 백스톱으로 등장.

## 학습법 회고
- **잘된 것**: 설계·결정은 학습자(중복주문 '둘 다', UNIQUE 직접 제안, default-deny 선택, 커밋 분리), 타이핑·검증은 코치 — #41→42 "바꿀 것" 유지. "주문 여러 개 불가능한 걸로 아는데 확인해줘"로 코치 주장 검증을 요구한 무브가 특히 좋았음(스키마 직접 확인 → 회귀 확정).
- **바꿀 것(다음 타임)**: 적용률 높음 = 컴포트존 신호. 다음 엣지는 *코드 적기 전에 개념을 먼저 입으로 인출*하는 게이트 한 칸 추가. 이번엔 default-deny·멱등 3중을 *결정·적용*은 했지만 "왜 안전한가/어떤 실패를 막나"를 먼저 말로 인출하진 않음. 특히 멱등/unique는 #36·#31 재방문이라 "전에 어디서 했지?"부터 했어야(volley-back).

## 다음 사이클 키워드 (i+1)
- **트랜잭션 밖 외부호출 분리 + Saga 보상** — confirm()이 `@Transactional` 안에서 토스 호출 → 승인 후 후속 실패 시 돈↔상태 불일치(arc「능동적 실패」). 종류: 코드 적용.
- **reservation↔waiting 사이클 끊기** — 마지막 남은 사이클, DIP 역전. 종류: 코드 적용.
- **ArchUnit 가드** — 사이클 1건뿐 → "신규 사이클 금지" 박기 좋은 시점. 종류: 코드 적용.
