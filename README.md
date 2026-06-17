# 🚀 사이클2 - 미션 (예약 대기 승인)

## 1단계 - 예약 대기 승인 [기능 목록]

- [x] 예약 취소 시 대기 1번이 자동으로 예약으로 전환 (자동 전환 방식 선택)
  - [x] 과거 슬롯이면 승격 생략
  - [x] 이중 취소 시 이중 승격 방지
- [x] 대기 전환 또는 취소 시 해당 슬롯의 대기 순번 재정렬
- [x] 관리자/매니저 대기 목록 조회 및 강제 취소

## API 명세
### 사용자 API

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /waitings | 대기 신청 | 로그인 |
| GET | /waitings | 내 대기 목록 조회 | 로그인 |
| DELETE | /waitings/{id} | 본인 대기 취소 | 로그인 |

**POST /waitings 요청 본문**
```json
{
  "date": "2025-06-10",
  "timeId": 1,
  "themeId": 1,
  "storeId": 1
}
```

**GET /waitings 응답 예시**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "date": "2025-06-10",
    "status": "WAITING",
    "rank": 1,
    "theme": { "id": 1, "name": "미스터리", "description": "...", "thumbnail": "..." },
    "time": { "id": 1, "startAt": "10:00" }
  }
]
```

### 관리자 API

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /admin/waitings | 전체 대기 목록 조회 | 관리자 |
| DELETE | /admin/waitings/{id} | 대기 강제 취소 | 관리자 |

### 매니저 API

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /manager/waitings | 소속 매장 대기 목록 조회 | 매니저 |
| DELETE | /manager/waitings/{id} | 소속 매장 대기 강제 취소 | 매니저 |

---

# 💳 사이클3 - 미션 (토스페이먼츠 결제 연동)

예약 생성 흐름에 외부 결제(Toss Payments)를 끼워, **결제가 성공해야 예약이 확정**되게 한다. 결제 로직을 직접 구현하지 않고 승인 API(`POST /v1/payments/confirm`)를 RestClient로 호출하며, **포트 & 어댑터(부패 방지 계층, ACL)**로 감싸 도메인이 Toss에 결합되지 않게 한다.

## 기능 목록

- [x] 예약 생성 시 결제 인증 전 주문(orderId·최종 amount) 저장, 예약은 결제 대기(PENDING)로 생성
- [x] 브라우저 Toss 결제창 SDK 연동 (클라이언트 키)
- [x] successUrl 콜백 — 저장 금액과 대조(검증) 후 승인, **조작된 금액은 승인 호출 전에 차단**(게이트웨이 미호출)
- [x] 결제 승인 API RestClient 호출 (Basic 인증 = `base64(secretKey + ":")`, 시크릿 키 외부화)
- [x] 포트 & 어댑터: `PaymentGateway`(포트) + `TossPaymentGateway`(어댑터) — Toss DTO는 어댑터 밖으로 새지 않음
- [x] 토스 에러 응답({code, message})을 예외로 매핑, 전용 `@RestControllerAdvice`로 처리 (미정의 코드는 기본 예외)
- [x] failUrl 처리 — 실패 사유 표시 + 결제 대기 주문/예약 정리 (취소 시 orderId 없을 수 있어 null 가드)
- [x] 콜백 소유권 검증 (타인 주문 확정/취소 차단)
- [x] 미결제 PENDING 만료 정리 스케줄러 (결제창 닫고 사라진 주문 회수)

## 결제 흐름

1. `POST /reservations` → 예약 PENDING 생성 + 주문(orderId·amount) 저장 → 프론트에 결제 정보 반환
2. 브라우저가 Toss 결제창으로 인증 → successUrl / failUrl로 복귀
3. `POST /payments/confirm` → 저장 금액과 대조 → 승인 호출 → paymentKey 저장, 예약 **BOOKED 확정**
4. (실패/취소) `POST /payments/fail` → 결제 대기 주문/예약 정리

## API 명세

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /reservations | 예약 생성(PENDING) + 주문 저장, 결제 정보 반환 | 로그인 |
| GET | /payments/config | 결제창용 클라이언트 키 조회 | - |
| POST | /payments/confirm | 금액 검증 후 결제 승인 → 예약 확정 | 로그인 |
| POST | /payments/fail | 결제 실패/취소 시 주문·예약 정리 | 로그인 |

**POST /reservations 응답 예시**
```json
{
  "reservationId": 1,
  "orderId": "a1b2c3d4e5f6...",
  "amount": 30000,
  "orderName": "냥이 점집"
}
```

**POST /payments/confirm 요청 본문**
```json
{
  "paymentKey": "tgen_20240...",
  "orderId": "a1b2c3d4e5f6...",
  "amount": 30000
}
```

## 설정 (실행 전 필수)

시크릿 키는 코드에 하드코딩하지 않고 **환경변수로 주입**한다. (`application.yml`의 `toss.secret-key`가 `${TOSS_SECRET_KEY:}`로 외부화됨)

```bash
TOSS_SECRET_KEY=test_sk_...   # 서버 승인 전용 비밀 키 (필수)
TOSS_CLIENT_KEY=test_ck_...   # 결제창용 공개 키
```

- 미결제 만료 정리: `payment.expiry.ttl-minutes`(기본 30분, 결제창 유효시간보다 길게), `payment.expiry.poll-interval-ms`(기본 60초).

