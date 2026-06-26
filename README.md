## 실행 환경 설정

JWT 서명에 사용할 비밀 키는 소스에 하드코딩하지 않고 외부에서 주입합니다.

### 빠른 시작

프로젝트 루트에 `.env` 파일을 만들고 다음 내용을 채웁니다.

```properties
JWT_SECRET_KEY=<openssl rand -base64 32로 생성한 값>
JWT_EXPIRE_LENGTH=3600000
```

이후 평소처럼 실행합니다.

```bash
./gradlew bootRun
```

### JWT 키 생성

```bash
openssl rand -base64 32
```

### 환경 변수 명세

| 변수                  | 필수 | 기본값       | 설명                                       |
|---------------------|----|-----------|------------------------------------------|
| `JWT_SECRET_KEY`    | 필수 | —         | HS256 서명에 사용할 Base64 인코딩 키 (32바이트 이상 권장) |
| `JWT_EXPIRE_LENGTH` | 선택 | `3600000` | 토큰 만료 시간 (ms)                            |

`.env`는 `.gitignore`에 포함되어 커밋되지 않습니다.
값이 누락되면 앱 시작이 실패(fail-fast)합니다.

운영 환경에서는 `.env` 대신 OS 환경 변수 또는 시크릿 매니저(AWS Secrets Manager,
HashiCorp Vault 등)를 사용합니다. `spring.config.import`의 `optional:` 접두사
덕분에 `.env` 파일 없이 OS 환경 변수만으로도 동작합니다.

### 테스트 실행

```bash
./gradlew test
```

테스트는 별도 profile(`application-test.properties`)의 고정 키를 사용하므로
`.env` 설정 없이 실행 가능합니다.

#### 실행 및 확인 방법

- 애플리케이션 실행

```bash
./gradlew bootRun
```

- 사용자 예약 화면 접속

```text
http://localhost:8080
```

- H2 Console 접속

```text
http://localhost:8080/h2-console
```

- H2 접속 정보

```text
JDBC URL: jdbc:h2:mem:database
User Name: sa
Password: 비워두기
```

- 화면 확인용 초기 데이터는 `src/main/resources/data.sql`에 정의되어 있다.

## 기능명세서

### **1단계 - 예약 대기 신청/취소**

- [x] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에**대기를 신청**할 수 있다.
- [x] 같은 슬롯에 대한 대기는**신청 순서대로 순번**이 부여된다.
- [x] 같은 사용자가 같은 슬롯에**중복 대기할 수 없다**.
- [x] 이미 지난 예약은 대기를 신청할 수 없다.
- [x] 사용자는 본인의**대기를 취소**할 수 있다.
- [x] 예약대기가 있는 예약 삭제를 한 경우 최근 예약대기자 정보로 예약된다. 
- [ ] [선택] 한 예약은 최대 20개의 예약 대기만 받을 수 있다.

### **2단계 - 내 예약 목록 조회 (상태 구분)**

- [x] 이전 미션의 내 예약 목록 조회를**확장**한다.
- [x] 사용자의**예약과 대기가 상태로 구분**되어 함께 표시된다.
- [x] 대기에는 본인의**대기 순번**도 함께 보여준다.

## 미션 중 기록

<a id="stuck-points"></a>
### 막힌 부분

<a id="stuck-normalization"></a>
#### 1. member와 store 관계의 정규화 vs 반정규화

현재 member과 그 중 매니저가 관리하는 매장(store)의 스키마를 다음과 같이 정의하였다.

```sql
-- 

CREATE TABLE member
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL, .
    .
    .
    store_id
    INT
    NOT
    NULL
)
```

```sql
CREATE TABLE member
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL, 
    store_id INT          NOT NULL
);

CREATE TABLE store_manager
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    store_id  INT NOT NULL,
    PRIMARY KEY (member_id)
);

CREATE TABLE store
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);
```

정규화를 하는 것이 불필요한 null 값을 줄일 수 있다는 의견과 구현의 간단함을 위해 반정규화를 해야한다는 의견이 대립했다.
상의 끝에 null 값이 많아도 메모리가 차지하는 비율이 적다는 점, JOIN과 코드 복잡도가 늘어난다는 점에서 반정규화를 채택했다.

<a id="stuck-wait-order"></a>
#### 2. 대기 순번 - 데이터로 저장할 것인가, 조회 시 계산할 것인가

같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다는 요구사항이 있다.
이 순번을 어떻게 다룰지에 대해 두 가지 방식이 대립했다.

**방식 A: `wait_order` 컬럼을 두고 INSERT 시점에 직접 채워 넣기**

```sql
CREATE TABLE reservation_wait (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    member_id      BIGINT NOT NULL,
    wait_order     INT    NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- INSERT 시 `SELECT MAX(wait_order) + 1 FROM reservation_wait WHERE reservation_id = ?` 로 채움
- 취소 시 자기 뒤의 모든 row의 `wait_order`를 1씩 감소시켜야 함

**방식 B: `created_at` 정렬로 조회 시점에 계산 (`ROW_NUMBER()`)**

```sql
SELECT id, member_id,
       ROW_NUMBER() OVER (PARTITION BY reservation_id ORDER BY created_at) AS wait_order
FROM reservation_wait
WHERE reservation_id = ?
```

저장 시에는 순번을 신경 쓰지 않고, 조회할 때만 정렬 기반으로 도출.

|              | 방식 A (저장)              | 방식 B (계산)             |
|--------------|------------------------|-----------------------|
| 조회 비용        | 낮음                     | 중간 (윈도 함수)            |
| 쓰기 복잡도       | 높음 (MAX+1, 동시성 위험)     | 낮음 (INSERT만)          |
| 취소 처리        | 뒤 row 모두 UPDATE        | 자기 row만 DELETE        |
| 동시성 안전성      | 어려움 (락 / unique + 재시도) | 자연스럽게 안전              |
| 데이터 일관성      | 컬럼 값과 정렬이 어긋날 위험       | `created_at` 단일 진실원   |

상의 끝에 **방식 B**를 채택했다. 순번은 화면에 노출되는 **파생 값**이지 핵심 데이터가 아니라는 점,
쓰기 경로의 동시성 리스크와 취소 시 부수 UPDATE를 피할 수 있다는 점,
진실원(`created_at`)이 하나로 유지된다는 점에서 단순성과 안전성이 모두 더 컸다.

<a id="stuck-db-vs-app"></a>
#### 3. 어떤 로직을 DB에서, 어떤 로직을 애플리케이션에서 처리할 것인가

위 결정 과정에서 자연스럽게 "이 계산은 어디서 하는 게 자연스러운가"라는 더 일반적인 물음에 부딪쳤다.
다음과 같이 기준을 정리했다.

**DB에서 처리하는 게 자연스러운 경우**

- 정렬·집계·윈도 함수처럼 데이터 전체에 대한 set 연산이 필요할 때 (예: 대기 순번, 인기 테마 카운트)
- 트랜잭션 경계 안에서 원자적으로 처리되어야 할 때 (예: unique 제약 위반 감지)
- 데이터를 애플리케이션으로 전부 끌어오면 비효율적일 때

**애플리케이션에서 처리하는 게 자연스러운 경우**

- 도메인 규칙이 데이터 외부 컨텍스트(현재 시각, 로그인 사용자 등)에 의존할 때 (예: `Reservation.isPast()`)
- 사용하는 DB 인프라가 해당 함수를 지원하지 않거나 이식성을 해칠 때 (예: H2와 운영 DB의 함수 차이)
- 도메인 객체의 자기 책임으로 표현하는 게 의미상 더 명확할 때

기준: **"SQL이 잘하는 일은 SQL에 맡기고, 도메인 규칙은 자바에 둔다."**

## Toss Payments 테스트 설정

결제창에는 클라이언트 키, 서버 승인 API에는 시크릿 키를 사용한다. 실제 키는 커밋하지 않고 프로젝트 루트의 `.env` 또는 실행 환경변수로 주입한다.

```properties
TOSS_CLIENT_KEY=test_ck_...
TOSS_SECRET_KEY=test_sk_...
TOSS_BASE_URL=https://api.tosspayments.com
RESERVATION_PAYMENT_AMOUNT=50000
```

예약 생성 시 서버가 `orderId`와 금액을 저장하고 예약을 `PENDING`으로 만든다. 브라우저 인증이 끝난 후 서버 승인까지 성공해야 `CONFIRMED` 예약과 생성 이력이 남는다.

실제 샌드박스 어댑터 테스트는 기본 테스트에서 제외된다. `RUN_REAL_API=true`와 `TOSS_SECRET_KEY`를 주입하면 오류 응답 변환을 확인할 수 있고, 브라우저 인증 결과인 `TOSS_TEST_PAYMENT_KEY`, `TOSS_TEST_ORDER_ID`, `TOSS_TEST_AMOUNT`까지 주입하면 성공 승인도 검증한다.

## 결과 불명확 결제와 회복 흐름 (확인 필요 상태)

토스 confirm 호출이 read timeout으로 끊겼을 때 "이 결제가 처리됐는지 아닌지"를 우리 서버가 알 수 없다. "결제 실패"로 단정 짓지 않는다 — 결과가 불명확한 상태를 별도로 표현해 사용자가 회복할 수 있도록 만든다.

### 상태와 전이

`PaymentOrderStatus`는 `READY → UNCONFIRMED → DONE` 흐름을 갖는다.

- **READY** — 결제 시도 전. 사용자가 결제창에서 진행하기 전.
- **UNCONFIRMED** — 토스 응답을 못 받아 결과 미확정. 클라이언트가 보낸 `paymentKey`는 저장돼 회복용 식별자로 쓰인다.
- **DONE** — 토스가 승인을 확인해 줬고, 예약은 `CONFIRMED`. 이력 1건 기록.

### 자동 재시도 + 트랜잭션 분리

`PaymentService.confirm`은 외부 호출을 트랜잭션 밖으로 빼고 `TransactionTemplate`로 세 구간을 나눈다.

1. `prepareConfirm` — 잠금 조회, 소유·금액 검증, 이미 DONE이면 멱등 단축.
2. 외부 호출 — `PaymentGatewayResponseTimeoutException`이면 지수 백오프(500ms → 1s → 2s)로 최대 3회 시도. `Idempotency-Key`(`orderId`)가 함께 박혀 이중 승인은 토스 측에서도 차단된다.
3. 결과 분기 — 성공이면 `finalizeConfirm`으로 DONE 전이 + 예약 CONFIRMED + 이력 기록. 끝까지 timeout이면 `markUnconfirmed`로 `UNCONFIRMED` 상태와 `paymentKey`를 커밋하고 예외를 전파한다.

같은 트랜잭션 안에서 던지면 마킹도 함께 롤백되기 때문에 트랜잭션을 잘게 쪼갠다. `TransactionTemplate`은 Spring 프록시를 거치지 않아 한 클래스 안에서 self-invocation 함정 없이 안전하게 분리된다.

### 사용자 회복 동선

"내 예약" 페이지(`GET /api/v1/reservations`) 응답이 결제 정보를 함께 내려준다(`orderId`, `paymentKey`, `amount`, `paymentStatus`).

- `paymentStatus = DONE` → "결제 완료" pill.
- `paymentStatus = UNCONFIRMED` → "확인 필요" pill + ↻ "다시 확인" 버튼.
- `paymentStatus = READY` → "결제 대기" pill.

"다시 확인"은 같은 `paymentKey` / `orderId` / `amount`로 `POST /api/v1/payments/confirm`을 재호출한다. 토스 측 멱등성과 `Idempotency-Key`로 이중 청구가 발생하지 않으며, 우리 서버는 `confirmAfterRecovery`로 `UNCONFIRMED → DONE` 전이 후 예약을 `CONFIRMED`로 마무리한다.

### 한계와 후속

`UNCONFIRMED`인데 `paymentKey`가 비어 있는 경우(클라이언트가 키를 받기도 전에 끊긴 흐름)는 자동 회복 대상이 아니다. 화면에서 "다시 확인" 버튼이 비활성화되며, 사용자는 결제창에서 다시 진행하거나 운영자가 수동 정정한다. 결제 조회 API를 도입하는 다음 사이클에서, `UNCONFIRMED` 주문의 실제 상태를 토스에 직접 물어 자동 정정하는 보상 흐름으로 확장할 수 있다.

## Rate Limit — 양방향 토큰 버킷

3단계는 호출량 상한(Rate Limit)을 다룬다. **알고리즘은 하나(토큰 버킷), 적용 방향은 둘**이다.

### 들어오는 요청 (서버 입장)

`RateLimitInterceptor`가 결제·예약 엔드포인트(`/api/v1/payments/**`, `/api/v1/reservations/**`)에 걸린다. `preHandle`에서 토큰을 소비하지 못하면 컨트롤러를 호출하지 않고 `429` + `Retry-After` 헤더로 거부한다. `capacity`(허용 버스트), `refill-per-sec`(평균 TPS 상한)은 `rate-limit.*`로 외부화.

### 나가는 호출 (클라이언트 입장)

토스 호출용 `RestClient`에 두 인터셉터를 차례로 등록.

1. **`OutboundRateLimitInterceptor`** — 호출 전 토큰을 소비. 한도 초과면 외부로 보내지 않고 `OutboundRateLimitException`(HTTP 429)으로 즉시 거부.
2. **`RetryAfterInterceptor`** — 응답이 `429`면 `Retry-After`(초)만큼 대기 후 재시도. 헤더가 없으면 1초 폴백. `maxAttempts` 회 초과시 마지막 응답 그대로 반환.

들어오는·나가는 쪽은 같은 `TokenBucketRateLimiter` 클래스를 재사용하되 **버킷 인스턴스는 분리**된다(다른 빈, 다른 정책). 나가는 한도는 토스가 우리에게 허용한 몫을 우리가 스스로 지키는 자기 통제. `outbound-rate-limit.*`로 외부화.

### 핵심 설계 결정

- **재시도 시 멱등키 유지**: 2단계의 `Idempotency-Key`(`orderId`)는 그대로 유지. read timeout(됐는지 모름)과 달리 `429`는 "아직 처리 안 됨"이라 본질적으로 재시도 안전하지만, 같은 키로 보내야 이전 사이클의 안전망과 결합된다.
- **인터셉터 등록 순서**: outbound → retry. 첫 호출은 토큰 1개 소비. 그 후 `429` 받아 재시도하는 호출은 같은 인터셉터 체인을 거치지 않으므로 추가 토큰을 소비하지 않는다.
- **fail-fast vs 블로킹 대기**: 나가는 한도 초과 시 즉시 거부(`OutboundRateLimitException`). 토큰이 찰 때까지 블로킹 대기시키면 스레드를 잡고 결정적 테스트가 어려워진다.
- **사용자별 분리는 후속**: 현재는 전역 버킷 하나. 운영에선 사용자/IP별 분리가 일반적이지만 이번 사이클 범위 밖.

### 결정적 테스트

`TokenBucketRateLimiter`는 `LongSupplier` 가짜 시계를 주입받아 시간 의존 로직을 결정적으로 검증한다. `Thread.sleep` 없는 단위 테스트로 capacity·refill·동시성을 한 번에 확인 가능.
