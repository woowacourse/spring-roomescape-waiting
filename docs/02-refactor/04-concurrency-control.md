# 동시성 문제 분석과 제어 전략

---

## 결론: 지금 코드는 동시 요청에 안전하지 않다. 테스트로 증명하고 DB 락으로 막아야 한다

현재 `saveReservation`, `cancelReservation`은 조회 → 판단 → 변경 사이에 다른 트랜잭션이 끼어들 수 있다.
해결 순서는 **테스트 먼저 → 실패 확인 → 락 적용 → 테스트 통과** 순서로 진행한다.

---

## 문제 인식 — 무엇이 문제였나?

**대상:** `saveReservation`, `cancelReservation` in `ReservationService`

### 문제 상황

**시나리오 1 — 동시 예약 (saveReservation)**

```
A: findWithReservations(slotId) → reservations 비어 있음 확인
B: findWithReservations(slotId) → reservations 비어 있음 확인 (A가 아직 커밋 안 함)
A: addReservation("A") → CONFIRMED, is_reserved = true → save
B: addReservation("B") → CONFIRMED, is_reserved = true → save  ← 둘 다 CONFIRMED
```

같은 슬롯에 CONFIRMED 예약이 2개 생긴다.

**시나리오 2 — 동시 취소 (cancelReservation)**

```
A: findWithReservations(reservationId) → CONFIRMED 상태 확인
B: findWithReservations(reservationId) → CONFIRMED 상태 확인 (A가 아직 커밋 안 함)
A: cancelReservation → CANCELLED, is_reserved = false
B: cancelReservation → CANCELLED (이미 취소된 예약을 또 취소) ← 예외 또는 불일치
```

### 문제가 되는 코드 / 구조

```java
// saveReservation — 조회와 변경 사이에 다른 트랜잭션이 끼어들 수 있다
ThemeSlot themeSlot = themeSlotRepository.findWithReservations(themeSlotId); // ← 이 시점
// ↑ 여기서 다른 트랜잭션이 같은 슬롯을 읽어도 막을 방법이 없다
Reservation reservation = themeSlot.addReservation(name);
themeSlotRepository.update(themeSlot);
reservationRepository.save(reservation);
```

### 문제 유형 체크

- [ ] 중복 코드 / 로직
- [ ] 불명확한 이름
- [ ] 단일 책임 원칙(SRP) 위반
- [x] 높은 결합도 — 조회·판단·변경이 한 트랜잭션 안에 있지만 격리가 안 됨
- [ ] 낮은 응집도
- [ ] 확장하기 어려운 구조
- [x] 테스트하기 어려운 구조 — 동시성 버그는 단순 단위 테스트로 재현 불가
- [ ] 성능 문제
- [x] 가독성 / 이해하기 어려움 — 락 없이는 코드만 봐서는 안전한지 알 수 없음
- [x] 기타: **Race Condition** — Check-Then-Act 패턴의 원자성 부재

---

## 1단계: 테스트 먼저 — 문제가 존재함을 증명한다

### 동시성 테스트가 Fake/Mock으로 불가능한 이유

Fake Repository는 단일 JVM 메모리 안에서 동작하므로 실제 DB 락이 없다.
동시성 버그는 **실제 DB + 실제 HTTP 요청 + 실제 격리 수준**이 있어야 재현된다.
따라서 동시성 테스트는 반드시 `@SpringBootTest(RANDOM_PORT)` + 실제 DB로 작성해야 한다.

### `CountDownLatch`로 동시성을 만드는 방법

`CountDownLatch`는 "거의 동시에" 출발하게 만드는 장치다.
JVM이 스레드 스케줄링을 보장하지 않으므로 100% 동시는 아니지만,
스레드가 많을수록 경쟁 조건이 발생할 확률이 높아진다.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/acceptance-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ConcurrencyTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("같은 빈 슬롯에 동시에 예약하면 정확히 하나만 CONFIRMED가 된다.")
    void 동시에_같은_슬롯에_예약하면_하나만_CONFIRMED가_된다() throws InterruptedException {
        // given — 미래의 빈 슬롯 ID 조회
        long themeSlotId = RestAssured.given()
                .when().get("/times?themeId=1&date=" + LocalDate.now().plusMonths(6))
                .then().statusCode(200)
                .extract().jsonPath().getLong("[0].id");

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);  // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<String> statuses = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            String name = "사용자" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();  // 모든 스레드가 여기서 대기
                    String status = RestAssured.given()
                            .contentType(ContentType.JSON)
                            .body(Map.of("name", name, "themeSlotId", themeSlotId))
                            .when().post("/reservations")
                            .then()
                            .extract().jsonPath().getString("status");
                    statuses.add(status);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();  // 동시 출발
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then — CONFIRMED는 정확히 1개여야 한다
        long confirmedCount = statuses.stream()
                .filter("CONFIRMED"::equals)
                .count();
        assertThat(confirmedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 CONFIRMED 예약에 동시에 취소를 보내면 한 번만 취소된다.")
    void 동시에_같은_예약을_취소하면_한_번만_처리된다() throws InterruptedException {
        // given — CONFIRMED 예약 ID (data.sql 기준 1번 예약)
        long reservationId = 1L;

        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Integer> statusCodes = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    int statusCode = RestAssured.given()
                            .when().patch("/reservations/" + reservationId + "/cancel")
                            .then().extract().statusCode();
                    statusCodes.add(statusCode);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then — 204(성공)는 정확히 1번, 나머지는 422(이미 취소됨)
        long successCount = statusCodes.stream().filter(c -> c == 204).count();
        assertThat(successCount).isEqualTo(1);
    }
}
```

### 테스트의 한계와 신뢰도 높이는 법

| 한계 | 대응 |
|---|---|
| JVM 스케줄러가 진짜 동시를 보장 못 함 | 스레드 수를 늘림 (5~10개), 테스트 횟수 반복 |
| 플리키 테스트(가끔만 실패) | `@RepeatedTest(10)`으로 10회 반복 실행 |
| H2는 락 모델이 MySQL과 달라서 재현 안 될 수 있음 | Testcontainers로 실제 MySQL을 띄워서 테스트 |

**락을 적용하기 전 테스트가 실패하는 것이 정상이다.**
테스트가 실패하면 "버그가 있다"는 증명이고, 락 적용 후 통과하면 "버그가 고쳐졌다"는 증명이다.

---

## 2단계: 동시성 제어 방법들

### 선택지 비교

| 방법 | 원리 | 장점 | 단점 |
|---|---|---|---|
| DB UNIQUE 제약 | INSERT 시 DB가 중복 차단 | 가장 단순, 락 오버헤드 없음 | 취소 후 재예약 같은 복잡한 규칙엔 부족 |
| 비관적 락 (`SELECT FOR UPDATE`) | 조회 시점에 행 잠금 | 강력한 보장, 직관적 | 대기 시간 증가, 데드락 가능 |
| 낙관적 락 (`version` 컬럼) | 커밋 시점에 버전 충돌 감지 | 락 경합 없음, 읽기 성능 좋음 | 충돌 시 재시도 로직 필요 |
| 격리 수준 조정 (`SERIALIZABLE`) | 트랜잭션 완전 직렬화 | 코드 변경 없음 | 성능 하락 심각, 과도한 락 |

**현재 코드에 가장 적합한 선택: 비관적 락 (`SELECT FOR UPDATE`)**

`saveReservation`과 `cancelReservation` 모두 "조회 → 판단 → 변경" 패턴이다.
조회 시점에 행을 잠가서 다른 트랜잭션이 같은 슬롯을 동시에 읽지 못하게 막는 것이 가장 직관적이다.

---

## 3단계: DB 레벨 구현

### 비관적 락 — `SELECT FOR UPDATE`

`findWithReservations` 쿼리에서 ThemeSlot을 조회할 때 잠근다.

```sql
-- JdbcThemeSlotRepository.findWithReservations() 수정
SELECT 
    ts.id AS id,
    th.id AS theme_id,
    ...
FROM theme_slot ts
    INNER JOIN time t ON ts.time_id = t.id
    INNER JOIN theme th ON ts.theme_id = th.id
WHERE ts.id = ?
FOR UPDATE  -- ← 이 한 줄이 핵심
```

`FOR UPDATE`가 붙으면:
- 이 트랜잭션이 끝날 때까지 다른 트랜잭션은 같은 행을 읽거나 쓸 수 없다
- 다른 트랜잭션은 이 트랜잭션이 커밋/롤백될 때까지 대기한다

Java 코드에서는:

```java
// ThemeSlotRepository 인터페이스에 추가
Optional<ThemeSlot> findWithReservationsForUpdate(Long themeSlotId);

// JdbcThemeSlotRepository 구현
@Override
public Optional<ThemeSlot> findWithReservationsForUpdate(Long themeSlotId) {
    // findById 쿼리에 FOR UPDATE 추가
    Optional<ThemeSlot> themeSlotOpt = jdbcTemplate.query("""
            SELECT ts.id AS id, ...
            FROM theme_slot ts ...
            WHERE ts.id = ?
            FOR UPDATE
            """, rowMapper(), themeSlotId).stream().findFirst();

    // 이후 Reservation 조회 및 ThemeSlot 조립 (findWithReservations와 동일)
    ...
}
```

`ReservationService`에서 쓰기 작업에만 `ForUpdate` 버전을 사용:

```java
@Transactional
public Reservation saveReservation(String name, Long themeSlotId) {
    ThemeSlot themeSlot = themeSlotRepository.findWithReservationsForUpdate(themeSlotId) // ← 변경
            .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    validateBeforeDate(themeSlot);
    validateDateTime(themeSlot);
    Reservation reservation = themeSlot.addReservation(name);
    themeSlotRepository.update(themeSlot);
    return reservationRepository.save(reservation);
}

@Transactional
public void cancelReservation(Long reservationId) {
    Reservation reservation = getReservationOrElseThrow(reservationId);
    ThemeSlot themeSlot = themeSlotRepository.findWithReservationsForUpdate(reservation.getThemeSlotId()) // ← 변경
            .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    ...
}
```

**주의:** `FOR UPDATE`는 반드시 트랜잭션 안에서만 동작한다. `@Transactional`이 없으면 즉시 락이 해제되어 의미가 없다.

---

### 유니크 제약 — DB가 중복을 차단

복잡한 락 없이도 DB 제약으로 막을 수 있는 부분이 있다.
"한 슬롯에 CONFIRMED는 하나"를 DB가 직접 보장하게 할 수 있다.

```sql
-- 부분 유니크 인덱스 (MySQL 8.0+에서는 함수 인덱스나 generated column으로 구현)
-- theme_slot_id 당 status = 'CONFIRMED'인 row가 1개를 넘지 않도록
ALTER TABLE reservation
ADD UNIQUE INDEX uniq_slot_confirmed (theme_slot_id, status);
-- 단, 이 방법은 PENDING이 여러 개인 경우를 허용하지 못하므로 적합하지 않다
```

현재 데이터 모델에서는 완전한 유니크 제약보다 `FOR UPDATE`가 더 적합하다.

---

## 4단계: Spring 레벨 구현

### `@Transactional` 격리 수준

`@Transactional`의 기본 격리 수준은 `READ_COMMITTED`다.
이 상태에서는 커밋된 데이터만 읽으므로 Phantom Read, Non-Repeatable Read 문제가 생길 수 있다.

```java
// SERIALIZABLE로 올리면 완전히 직렬화되지만 성능 하락이 심각하다 — 권장하지 않음
@Transactional(isolation = Isolation.SERIALIZABLE)
public Reservation saveReservation(...) { ... }

// FOR UPDATE와 기본 격리 수준 조합이 현실적인 선택이다
@Transactional  // isolation = READ_COMMITTED (기본값)
public Reservation saveReservation(...) {
    ThemeSlot themeSlot = themeSlotRepository.findWithReservationsForUpdate(themeSlotId);
    // ↑ FOR UPDATE가 행 잠금을 담당, 트랜잭션이 커밋될 때 락 해제
    ...
}
```

### `completeReservation`에 `@Transactional` 추가

현재 `completeReservation`은 `@Transactional`이 없다.
쓰기 작업임에도 트랜잭션 보호가 없어 중간 실패 시 불일치가 생길 수 있다.

```java
// Before
public void completeReservation(Long reservationId) { ... }

// After
@Transactional
public void completeReservation(Long reservationId) { ... }
```

---

## 변경 범위 요약

| 파일 | 변경 내용 |
|---|---|
| `ThemeSlotRepository` | `findWithReservationsForUpdate(Long id)` 추가 |
| `JdbcThemeSlotRepository` | `FOR UPDATE` 쿼리 구현 |
| `FakeThemeSlotRepository` | `findWithReservations` 재사용 (테스트에서 락 의미 없음) |
| `ReservationService` | `saveReservation`, `cancelReservation` → `ForUpdate` 버전 사용 |
| `ReservationService` | `completeReservation` → `@Transactional` 추가 |
| `ConcurrencyTest` (신설) | 동시 예약 / 동시 취소 테스트 |

---

## 회고

### 배운 점

- **테스트 먼저 작성하는 이유**: 락을 먼저 추가하면 버그가 있었는지조차 증명할 수 없다. 테스트가 실패하는 것을 먼저 확인하고 락을 추가해야 "이 락이 이 버그를 막는다"는 인과관계를 증명할 수 있다.

- **Fake로는 동시성 테스트가 불가능하다**: Fake Repository는 실제 DB 락이 없어서 동시성 버그를 재현할 수 없다. 동시성은 반드시 `@SpringBootTest(RANDOM_PORT)` + 실제 DB(또는 Testcontainers)로 테스트해야 한다.

- **`CountDownLatch`는 확률적 도구다**: 100% 동시성을 보장하지 않는다. 스레드 수를 늘리고 `@RepeatedTest`로 반복 실행하면 신뢰도가 높아지지만 완전한 결정론적 테스트는 아니다. 플리키 테스트임을 알고 사용해야 한다.

- **`FOR UPDATE`는 반드시 `@Transactional` 안에서**: `FOR UPDATE`로 잠근 락은 트랜잭션이 끝날 때 해제된다. `@Transactional`이 없으면 조회 직후 락이 풀려 의미가 없다.

- **H2에서는 `FOR UPDATE`가 다르게 동작할 수 있다**: 동시성 테스트는 운영 DB(MySQL)와 같은 환경에서 해야 의미가 있다. 단위 테스트용 H2로는 검증이 불완전하다. Testcontainers가 이 간극을 메운다.
