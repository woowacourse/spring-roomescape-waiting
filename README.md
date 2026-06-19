## 실행 환경설정

결제 기능은 Toss Payments 시크릿 키가 필요하며, 이 키는 `application.properties`에 직접 적지 않고 환경변수로만 주입한다.

- `application.properties`의 `toss.secret-key=${TOSS_SECRET_KEY}`는 기본값이 없으므로, 아래 두 방법 중 하나로 키를 제공해야 한다.
  1. 환경변수 `TOSS_SECRET_KEY` 직접 설정
  2. `src/main/resources/application-local.properties` 파일을 만들어 `toss.secret-key=<로컬/샌드박스 시크릿 키>`를 적는다 (이 파일은 `.gitignore`에 등록되어 커밋되지 않는다)
- `spring.profiles.active`는 기본값이 `local`이므로(`application.properties` 참고), IDE Run 버튼/`./gradlew bootRun` 등 실행 방식과 무관하게 `application-local.properties`가 자동으로 적용된다. 운영 환경에서는 `SPRING_PROFILES_ACTIVE` 환경변수로 다른 profile을 지정한다.

## API

### 사용자 (`/api/*`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/reservations` | 예약 생성 |
| `POST` | `/api/reservations/waiting` | 대기 신청 |
| `PATCH` | `/api/reservations/entries/{id}` | 예약 변경 |
| `DELETE` | `/api/reservations/entries/{id}` | 예약/대기 취소 |
| `GET` | `/api/reservations/entries/{id}` | 예약 단건 조회 |
| `GET` | `/api/reservations` | 예약 검색 |
| `GET` | `/api/themes` | 활성 테마 목록 |
| `GET` | `/api/themes/{id}/times?date=` | 테마별 시간 슬롯 및 예약 가능 여부 조회 |
| `GET` | `/api/themes/popular` | 인기 테마 조회 |
| `POST` | `/api/payments/prepare` | 결제 준비(주문 생성, 슬롯 PENDING 선점) |

- 사용자 이름을 입력받아 예약 목록 및 정보를 조회할 수 있다.
- 예약 단건 조회 및 취소 시 사용자 권한 검증은 추가로 진행하지 않는다. (추후 개선 예정)

### 관리자 (`/api/admin/*`)

`role: ADMIN` 헤더 필요.

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/admin/reservations` | 예약 생성 |
| `DELETE` | `/api/admin/reservations/entries/{id}` | 예약/대기 취소 |
| `GET` | `/api/admin/reservations` | 전체 예약 목록 |
| `POST` | `/api/admin/times` | 예약 시간 등록 |
| `PATCH` | `/api/admin/times/{id}/activate` | 예약 시간 활성화 |
| `PATCH` | `/api/admin/times/{id}/deactivate` | 예약 시간 비활성화 |
| `GET` | `/api/admin/times` | 예약 시간 목록 |
| `POST` | `/api/admin/themes` | 테마 등록 |
| `PATCH` | `/api/admin/themes/{id}/activate` | 테마 활성화 |
| `PATCH` | `/api/admin/themes/{id}/deactivate` | 테마 비활성화 |
| `GET` | `/api/admin/themes` | 전체 테마 목록 |

### 응답 status 규칙

두 API는 요청 시점의 슬롯 상태에 따라 `entry.status`가 달라진다.  
클라이언트는 요청 엔드포인트가 아닌 **응답의 `entry.status`** 를 기준으로 결과를 판단해야 한다.

**`POST /api/reservations/waiting`**

| 슬롯 상태 | entry.status | 이유 |
|---|---|---|
| 예약자 없음 | `RESERVED` | 요청 처리 시점에 빈 자리가 생기면 대기 대신 예약으로 등록 |
| 예약자 있음 | `WAITING` | 대기 등록 |

**`PATCH /api/reservations/entries/{id}`**

| 대상 슬롯 상태 | entry.status | 이유 |
|---|---|---|
| 예약자 없음 | `RESERVED` | 예약으로 이동 |
| 예약자 있음 | `WAITING` | 대기로 이동 |

---

## 구현 기능

### 예약 대기
- 날짜 및 시간이 유효한 경우, 이미 예약이 존재하는 슬롯에 대기를 신청할 수 있다.
  - 날짜 및 시간이 과거인 경우 대기가 불가능하다.
  - 대기를 신청했는데 예약이 존재하지 않는다면 예약 상태로 등록한다.

- 같은 사용자가 같은 슬롯에 중복 대기 및 예약을 신청할 수 없다.

### 예약 대기 조회
- 사용자 이름을 사용하여 식별자를 포함한 예약 목록을 조회한다.
- 내 예약 목록 조회 시 예약 상태와 대기 상태를 구분한다.
- 대기 상태의 경우 대기 순번을 함께 표시한다.


### 예약 및 대기 취소
- 사용자는 본인의 대기를 취소할 수 있다.
  - 로그인 기능이 없는 상태이므로 예약 식별자를 전달하여 해당 예약을 취소한다.
- 예약이 취소된 경우 다음 대기 순번이 자동으로 예약된다.

### 결제 (Toss Payments 연동)
- 예약은 결제가 완료되어야 확정(`RESERVED`)된다.
  1. `POST /api/payments/prepare` — 슬롯을 `PENDING` 상태로 선점하고 주문번호(`orderId`)를 발급한다.
  2. `GET /payment/checkout` — Toss 결제위젯(주문서형) 페이지로 이동해 인증을 진행한다.
  3. 인증 성공 시 `GET /payments/success`에서 서버가 Toss 결제 승인 API를 직접 호출해 결제를 확정하고, 예약을 `PENDING → RESERVED`로 전환한다.
  4. 인증 실패/취소 시 `GET /payments/fail`에서 실패 사유를 보여주고 `PENDING` 상태였던 슬롯을 정리한다. `orderId`가 없는 사용자 취소(`PAY_PROCESS_CANCELED`)도 처리한다.
- 결제 승인 전에 클라이언트가 전달한 금액과 서버에 저장된 주문 금액을 대조해, 금액이 변조된 요청은 Toss 결제 승인 API를 호출하지 않고 즉시 차단한다.
- Toss가 반환하는 주요 에러코드(이미 처리된 결제, 카드 거절, 키 설정 오류, 재시도 대상 등)는 `TossPaymentException` 하위 도메인 예외로 매핑되며, 정의되지 않은 코드는 기본 예외로 처리된다.
- 결제 실패로 `PENDING` 엔트리가 정리되더라도 대기자가 결제 없이 자동으로 예약 확정되지 않으며, 이미 결제 완료(`RESERVED`)된 예약은 재취소되지 않는다.
- Toss 시크릿 키는 코드/설정 파일에 하드코딩하지 않고 환경변수로만 주입한다 (`실행 환경설정` 참고).
