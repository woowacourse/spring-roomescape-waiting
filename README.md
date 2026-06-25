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
