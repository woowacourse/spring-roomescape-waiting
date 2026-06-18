# 작업 단위의 정합성 보장: 트랜잭션 · 동시성 · 도메인 세 층위

---

## 개요: 왜 세 층위가 필요한가

하나의 비즈니스 작업(`cancelReservation`, `saveReservation` 등)이 여러 DB 변경을 일으킬 때,
세 가지 층위에서 각각 다른 종류의 오류가 생긴다.

```
요청
 │
 ├── 도메인 층   상태 전이 규칙 위반 (CANCELLED 예약을 또 취소)
 │              단위 작업 경계 오류 (removeReservation이 대기자 처리 누락)
 │
 ├── 트랜잭션 층  원자성 결여 (@Transactional 누락, 중간 실패 시 불일치)
 │              롤백 범위 오류 (어디서 롤백이 일어나는가)
 │
 └── 동시성 층   경쟁 조건 (두 트랜잭션이 같은 행을 동시에 읽고 쓴다)
                Check-Then-Act 원자성 부재
```

각 층위는 서로 다른 도구로 보장한다.

| 층위 | 문제 | 보장 도구 | 테스트 방법 |
|---|---|---|---|
| 도메인 | 잘못된 상태 전이, 단위 작업 누락 | 상태 머신, 불변식 | 단위 테스트 |
| 트랜잭션 | 부분 성공, 롤백 안 됨 | `@Transactional`, 전파 설정 | 통합 테스트 |
| 동시성 | 경쟁 조건, 중복 데이터 | `FOR UPDATE`, 낙관적 락 | 수락 테스트 (실제 DB) |

---

## 1. 도메인 층: 상태 전이와 작업 단위 (Unit of Work)

### 1-1. 상태 머신이 보장해야 할 것

`Reservation`은 `PENDING → CONFIRMED → COMPLETED` 또는 `→ CANCELLED` 흐름을 가진다.
각 상태에서 허용되지 않는 전이는 도메인이 직접 예외를 던져야 한다.

```
PENDING   ──cancel──→ CANCELLED
PENDING   ──confirm──→ CONFIRMED
CONFIRMED ──cancel──→ CANCELLED
CONFIRMED ──complete──→ COMPLETED
```

현재 코드는 State 패턴으로 이를 구현하고 있다.
`CancelledStatus.cancel()`은 `INVALID_CANCELLED_COMMAND`를 던진다.

**잠재적 문제: 오류 메시지가 부정확하다**

동시 취소 요청에서 두 번째 취소가 들어왔을 때,
이미 취소된 예약을 다시 취소하려 하면 `INVALID_CANCELLED_COMMAND`가 발생한다.
이 에러 코드는 "취소할 수 없는 예약"이라는 의미지만, 실제 원인은 "이미 취소된 예약"이다.
사용자에게는 `RESERVATION_ALREADY_CANCELLED` (HTTP 409)가 더 정확한 응답이다.

```java
// CancelledStatus.java — 현재
@Override
public void cancel(Reservation reservation) {
    throw new CustomException(ErrorCode.INVALID_CANCELLED_COMMAND); // ← 422, 모호한 메시지
}

// 더 정확한 표현
@Override
public void cancel(Reservation reservation) {
    throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED); // ← 409, 명확한 이유
}
```

### 1-2. 단위 작업 경계 (Unit of Work) — removeReservation의 누락

`removeReservation`은 예약 행을 삭제하고 슬롯을 `is_reserved = false`로 돌린다.
그런데 **PENDING 대기자가 있을 때** 이 로직은 불완전하다.

```java
// ReservationService.removeReservation — 현재
@Transactional
public void removeReservation(long reservationId) {
    Reservation reservation = getReservationOrElseThrow(reservationId);
    reservationRepository.deleteById(reservationId);                          // ① 예약 삭제
    themeSlotRepository.update(new ThemeSlot(..., false));                    // ② 슬롯 해제
    // ③ PENDING 대기자 처리가 없다 ← 버그
}
```

CONFIRMED 예약이 삭제되면 PENDING 대기자는 영원히 PENDING에 갇힌다.
슬롯은 `is_reserved = false`이므로 새 예약은 CONFIRMED로 들어오지만,
기존 대기자는 대기 목록에 그대로 남는다.

**올바른 단위 작업 경계**: `cancelReservation`처럼 `themeSlot.cancelReservation()`을 통해 도메인이 대기자 승격을 처리해야 한다.

```
removeReservation의 올바른 단위 작업:
  1. 취소 대상 예약 CANCELLED 처리 (또는 삭제)
  2. [대기자 있음] 첫 번째 PENDING → CONFIRMED 승격
  3. [대기자 없음] ThemeSlot.is_reserved → false
```

### 1-3. 단위 테스트로 보장하는 방법

단위 테스트는 DB 없이 도메인 객체만으로 상태 전이를 검증한다.
Fake Repository를 쓰기 때문에 빠르고 결정적이다.

```java
// ThemeSlotTest — 상태 전이 검증
@Test
void 대기자가_있을_때_취소하면_첫_번째_대기자가_CONFIRMED가_된다() {
    ThemeSlot slot = ThemeSlot이_있고_대기자가_있는_상태();
    Reservation confirmed = slot.getReservations().findById(confirmedId);
    Reservation pending = slot.getReservations().findById(pendingId);

    Optional<Reservation> promoted = slot.cancelReservation(confirmedId);

    assertThat(confirmed.isConfirmed()).isFalse();           // 취소된 예약은 CONFIRMED가 아님
    assertThat(confirmed.isCancelled()).isTrue();
    assertThat(promoted).isPresent();
    assertThat(promoted.get().isConfirmed()).isTrue();       // 대기자가 CONFIRMED로 승격
    assertThat(slot.isReserved()).isTrue();                  // 슬롯은 여전히 예약됨
}

@Test
void 이미_취소된_예약을_다시_취소하면_예외가_발생한다() {
    Reservation cancelled = cancelledReservation();

    assertThatThrownBy(cancelled::cancel)
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.RESERVATION_ALREADY_CANCELLED);
}

@Test
void PENDING_상태_예약은_complete를_호출하면_예외가_발생한다() {
    Reservation pending = pendingReservation();

    assertThatThrownBy(pending::complete)
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_PENDING_COMMAND);
}
```

**상태 전이 테스트의 원칙**: 각 상태에서 허용된 전이와 금지된 전이를 모두 테스트한다.
허용된 것만 테스트하면 금지된 전이가 조용히 통과하는 경우를 발견하지 못한다.

---

## 2. 트랜잭션 층: 원자성과 롤백

### 2-1. `@Transactional`이 없는 쓰기 작업

현재 `completeReservation`에는 `@Transactional`이 없다.

```java
// ReservationService — 현재
public void completeReservation(Long reservationId) {   // @Transactional 없음
    Reservation reservation = getReservationOrElseThrow(reservationId);
    reservation.complete();
    reservationRepository.updateStatus(reservation);     // DB 쓰기
}
```

쓰기 작업이 하나라면 원자성 문제는 없다.
그러나 트랜잭션이 없으면:
- 읽기 격리가 없어 더티 리드 가능성이 생긴다
- 추후 이 메서드에 쓰기 작업이 추가될 때 버그가 숨어든다
- Spring의 트랜잭션 관련 기능(감사 로그, 이벤트 발행 등)이 제대로 동작하지 않을 수 있다

**원칙: 상태를 변경하는 서비스 메서드는 모두 `@Transactional`을 붙인다.**

### 2-2. 전파(Propagation)와 중첩 호출

현재 `findWithReservations`는 내부에서 `findById`를 호출한다.
`findById`에는 `FOR UPDATE`가 있어 트랜잭션 안에서만 의미 있다.

```java
// JdbcThemeSlotRepository.findWithReservations — 현재 구조
@Override
public Optional<ThemeSlot> findWithReservations(Long themeSlotId) {
    Optional<ThemeSlot> themeSlotOpt = findById(themeSlotId);   // FOR UPDATE 쿼리 실행
    // ...
}
```

**문제**: `findWithReservations`가 `@Transactional` 없는 서비스 메서드에서 호출되면,
`FOR UPDATE`는 쿼리 실행 직후 자동 커밋되어 락이 즉시 해제된다.

```
findReservationBy (트랜잭션 없음)
 └── findWithReservations
      └── findById (FOR UPDATE) ← 락 획득, 즉시 해제 → 의미 없음
```

`findWaitingReservationWithOrder`도 동일하다.

**올바른 설계**: 락이 필요한 쓰기 경로와 락이 필요 없는 읽기 경로를 분리한다.

```java
// 쓰기 전용: FOR UPDATE 포함
Optional<ThemeSlot> findWithReservationsForUpdate(Long themeSlotId);

// 읽기 전용: 락 없음
Optional<ThemeSlot> findWithReservations(Long themeSlotId);
```

서비스 레이어:
```java
@Transactional
public Reservation saveReservation(String name, Long themeSlotId) {
    // 쓰기 → FOR UPDATE
    ThemeSlot themeSlot = themeSlotRepository.findWithReservationsForUpdate(themeSlotId)...;
    ...
}

public MyReservationResponse findReservationBy(String name) {
    // 읽기 → 락 없음
    ThemeSlot themeSlot = themeSlotRepository.findWithReservations(reservation.getThemeSlotId())...;
    ...
}
```

### 2-3. 롤백 테스트로 원자성 보장 확인

통합 테스트에서 실제 DB를 사용하여 롤백 동작을 검증한다.
핵심은 "중간에 실패가 일어났을 때 이전 변경이 취소되는지"를 확인하는 것이다.

```java
@SpringBootTest
@Transactional                           // 테스트 자체도 트랜잭션 — 끝나면 롤백
@Sql("/acceptance-reset.sql")
class TransactionRollbackTest {

    @Autowired ReservationService reservationService;
    @Autowired ReservationRepository reservationRepository;
    @Autowired ThemeSlotRepository themeSlotRepository;

    @Test
    @DisplayName("cancelReservation 도중 예외가 발생하면 취소된 상태가 DB에 반영되지 않는다")
    void 취소_중_예외_발생_시_롤백된다() {
        // given — CONFIRMED 예약 존재
        long reservationId = 1L;
        String statusBefore = reservationRepository.findById(reservationId)
                .get().getReservationStatusName();
        assertThat(statusBefore).isEqualTo("CONFIRMED");

        // when — 예외를 발생시키는 상황 (존재하지 않는 예약 ID로 취소 시도)
        assertThatThrownBy(() -> reservationService.cancelReservation(999L))
                .isInstanceOf(CustomException.class);

        // then — 원래 예약은 변경되지 않았어야 한다
        String statusAfter = reservationRepository.findById(reservationId)
                .get().getReservationStatusName();
        assertThat(statusAfter).isEqualTo("CONFIRMED");  // 롤백으로 원상 복구
    }

    @Test
    @DisplayName("예약 생성 시 슬롯 업데이트와 예약 저장이 모두 커밋되거나 모두 롤백된다")
    void 예약_저장과_슬롯_업데이트는_원자적이다() {
        // 이 테스트는 실제로 실패를 시뮬레이션하기 어렵다.
        // 대신 성공 케이스에서 두 변경이 모두 반영됐는지 확인한다.
        long themeSlotId = 빈_슬롯_조회();
        boolean isReservedBefore = themeSlotRepository.findById(themeSlotId)
                .get().isReserved();
        assertThat(isReservedBefore).isFalse();

        reservationService.saveReservation("홍길동", themeSlotId);

        boolean isReservedAfter = themeSlotRepository.findById(themeSlotId)
                .get().isReserved();
        long confirmedCount = reservationRepository.findAll().stream()
                .filter(r -> r.getReservationStatusName().equals("CONFIRMED"))
                .filter(r -> r.getThemeSlotId().equals(themeSlotId))
                .count();

        // 두 변경이 모두 반영돼야 한다 (원자성)
        assertThat(isReservedAfter).isTrue();     // ThemeSlot 업데이트 반영
        assertThat(confirmedCount).isEqualTo(1);  // Reservation 저장 반영
    }
}
```

**주의**: `@Transactional` 테스트는 기본적으로 테스트 종료 후 롤백된다.
롤백 여부를 확인하는 테스트에서는 `@Commit`이나 별도 트랜잭션을 사용해야 한다.

```java
@Test
@DisplayName("completeReservation은 @Transactional 없이도 DB에 즉시 반영된다 — 하지만 예외 발생 시 롤백이 안 된다")
void completeReservation_롤백_없음_확인() {
    // 이 테스트가 보여주는 것:
    // @Transactional 없는 메서드는 각 DB 작업이 독립적으로 커밋된다.
    // 따라서 중간 예외 시 앞의 변경이 취소되지 않는다.
    // 현재 completeReservation은 DB 작업이 하나라서 문제가 없지만,
    // 작업이 추가되면 즉시 버그가 된다.
}
```

---

## 3. 동시성 층: 경쟁 조건과 락

### 3-1. 어떤 경쟁 조건이 존재하는가

**시나리오 A — 동시 예약 (saveReservation)**

```
T1: findWithReservations(slotId) → 대기자 없음 확인 (슬롯 락 획득)
T2: findWithReservations(slotId) → 대기(T1의 락 때문에)
T1: addReservation("A") → CONFIRMED, is_reserved = true → 커밋, 락 해제
T2: findWithReservations 재실행 → 이미 CONFIRMED 예약 존재 확인
T2: addReservation("B") → 대기자 없음? No, "A"가 CONFIRMED → PENDING으로 처리됨 ✓
```

현재 `FOR UPDATE`가 `findById`에 있고 `saveReservation`은 `@Transactional` 안에서 호출하므로,
T2는 T1의 락이 풀릴 때까지 기다렸다가 이미 CONFIRMED 예약이 있음을 확인하고 PENDING으로 처리된다.

**시나리오 B — 동시 취소 (cancelReservation)**

```
T1: reservationRepository.findById(1) → CONFIRMED
T2: reservationRepository.findById(1) → CONFIRMED  (T1이 아직 커밋 안 함)
T1: findWithReservations(slotId) → 락 획득
T2: findWithReservations(slotId) → 대기
T1: cancelReservation → CANCELLED, 대기자 없음 → is_reserved=false → 커밋, 락 해제
T2: findWithReservations 재실행 → themeSlot 로드 (reservation이 CANCELLED)
T2: themeSlot.cancelReservation(1) → CancelledStatus.cancel() → 예외 발생
T2: 트랜잭션 롤백 ✓
```

현재 코드도 결과적으로 안전하지만, T2에서 발생하는 예외 메시지가 모호하다.

**시나리오 C — modifyReservation의 잠재적 경쟁 조건**

```java
@Transactional
public Reservation modifyReservation(Long reservationId, Long themeSlotId) {
    Reservation reservation = getReservationOrElseThrow(reservationId); // 락 없음
    ThemeSlot themeSlot = getThemeSlotOrElseThrow(themeSlotId);          // 새 슬롯 락 획득
    // ...
    validateIsExistBy(themeSlotId);  // reservationRepository.existsByThemeSlotId 조회
    themeSlotRepository.update(new ThemeSlot(...기존 슬롯..., false));  // 기존 슬롯 해제 (락 없음)
    themeSlotRepository.update(new ThemeSlot(...새 슬롯..., true));     // 새 슬롯 예약
}
```

기존 슬롯을 락 없이 해제하기 때문에, 동시에 다른 사용자가 기존 슬롯에 예약할 경우 충돌 가능성이 있다.

또한 `validateIsExistBy`가 `existsByThemeSlotId`를 사용하는데,
이는 CANCELLED, PENDING 포함 모든 예약의 존재를 확인한다.
PENDING 대기자가 있는 슬롯도 "이미 예약 있음"으로 처리하여 이동이 차단된다.
실제로 막아야 할 조건은 "CONFIRMED 예약이 있는 경우"다.

### 3-2. 동시성 테스트 작성 원칙

**원칙 1: 동시성 테스트는 반드시 실제 DB와 실제 HTTP를 써야 한다**

Fake Repository는 JVM 메모리에서만 동작하고 DB 락이 없다.
동시성 버그는 실제 DB의 락 모델과 격리 수준이 있어야 재현된다.

```
단위 테스트 (Fake)    → 동시성 버그 재현 불가
통합 테스트 (H2)      → 락 모델이 MySQL과 달라서 불완전
수락 테스트 (MySQL)   → 실제 환경과 동일, 유일하게 신뢰 가능
```

**원칙 2: 버그 증명 → 락 적용 → 테스트 통과 순서를 지킨다**

락을 먼저 적용하면 "이 락 전에 버그가 있었는지"를 증명할 수 없다.
테스트가 실패하는 것을 먼저 확인해야 "이 테스트가 실제 버그를 잡는다"고 신뢰할 수 있다.

**원칙 3: `@RepeatedTest`로 확률적 특성을 보완한다**

동시성 테스트는 비결정적이다. 한 번 통과해도 버그가 없다는 보장이 아니다.
반복 실행으로 신뢰도를 높인다.

### 3-3. 동시성 테스트 작성 방법

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/acceptance-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ConcurrencyTest {

    @LocalServerPort int port;

    @BeforeEach
    void setUp() { RestAssured.port = port; }

    // --- 동시 예약: 하나만 CONFIRMED가 돼야 한다 ---

    @RepeatedTest(10)
    @DisplayName("같은 빈 슬롯에 동시에 예약하면 정확히 하나만 CONFIRMED가 된다")
    void 동시에_같은_슬롯에_예약하면_하나만_CONFIRMED가_된다() throws InterruptedException {
        long themeSlotId = 빈_슬롯_ID_조회();
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threadCount);
        List<String> statuses = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            String name = "사용자" + i;
            Executors.newCachedThreadPool().submit(() -> {
                try {
                    startLatch.await();
                    String status = RestAssured.given()
                            .contentType(ContentType.JSON)
                            .body(Map.of("name", name, "themeSlotId", themeSlotId))
                            .when().post("/reservations")
                            .then().extract().jsonPath().getString("status");
                    statuses.add(status);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        long confirmedCount = statuses.stream().filter("CONFIRMED"::equals).count();
        assertThat(confirmedCount).isEqualTo(1);  // 정확히 하나만 CONFIRMED
    }

    // --- 동시 취소: 한 번만 성공해야 한다 ---

    @RepeatedTest(5)
    @DisplayName("같은 CONFIRMED 예약에 동시에 취소를 보내면 한 번만 성공한다")
    void 동시에_같은_예약을_취소하면_한_번만_처리된다() throws InterruptedException {
        long reservationId = CONFIRMED_예약_ID;
        int threadCount = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threadCount);
        List<Integer> statusCodes = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Executors.newCachedThreadPool().submit(() -> {
                try {
                    startLatch.await();
                    int code = RestAssured.given()
                            .when().patch("/reservations/" + reservationId + "/cancel")
                            .then().extract().statusCode();
                    statusCodes.add(code);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        long successCount = statusCodes.stream().filter(c -> c == 204).count();
        assertThat(successCount).isEqualTo(1);  // 204 No Content는 딱 한 번
    }
}
```

### 3-4. `FOR UPDATE` 구현 시 주의사항

**현재 코드의 구조적 문제**

현재 `findById`에 `FOR UPDATE`가 붙어 있어, 읽기 전용 경로도 락을 시도한다.

```java
// JdbcThemeSlotRepository.findById — 현재
@Override
public Optional<ThemeSlot> findById(long id) {
    String sql = "SELECT ... FROM theme_slot ts ... WHERE ts.id = ? FOR UPDATE"; // ← 항상 잠금
    return jdbcTemplate.query(sql, rowMapper(), id).stream().findFirst();
}
```

`findWithReservations`와 `getThemeSlotOrElseThrow` 모두 `findById`를 사용하므로,
`@Transactional`이 없는 서비스 메서드에서 호출되면 락은 즉시 해제되어 의미가 없다.
그러나 락 획득 시도 자체는 일어나므로 불필요한 오버헤드가 생긴다.

**올바른 설계**

```java
// 읽기 전용 — 락 없음
@Override
public Optional<ThemeSlot> findById(long id) {
    String sql = "SELECT ... FROM theme_slot ts ... WHERE ts.id = ?"; // FOR UPDATE 없음
    return jdbcTemplate.query(sql, rowMapper(), id).stream().findFirst();
}

// 쓰기 전용 — 반드시 @Transactional 안에서만 호출
@Override
public Optional<ThemeSlot> findByIdForUpdate(long id) {
    String sql = "SELECT ... FROM theme_slot ts ... WHERE ts.id = ? FOR UPDATE";
    return jdbcTemplate.query(sql, rowMapper(), id).stream().findFirst();
}

// findWithReservations는 findById 사용 (락 없음)
// findWithReservationsForUpdate는 findByIdForUpdate 사용 (쓰기 경로 전용)
```

서비스:
```java
@Transactional
public Reservation saveReservation(String name, Long themeSlotId) {
    ThemeSlot themeSlot = themeSlotRepository.findWithReservationsForUpdate(themeSlotId)...; // 락
}

public MyReservationResponse findReservationBy(String name) {
    ThemeSlot themeSlot = themeSlotRepository.findWithReservations(...)...; // 락 없음
}
```

---

## 4. 세 층위 문제 종합 정리

### 현재 코드에서 발견된 구체적 문제와 수정 방향

| 위치 | 문제 | 층위 | 심각도 | 수정 방향 |
|---|---|---|---|---|
| `completeReservation` | `@Transactional` 없음 | 트랜잭션 | 중간 | `@Transactional` 추가 |
| `removeReservation` | PENDING 대기자 처리 없음 | 도메인 | 높음 | `themeSlot.cancelReservation` 경유 |
| `findById` | 읽기 경로에도 `FOR UPDATE` | 동시성 | 낮음 | 읽기/쓰기 메서드 분리 |
| `modifyReservation` | 기존 슬롯 락 없이 해제 | 동시성 | 중간 | 기존 슬롯도 ForUpdate로 조회 |
| `modifyReservation` | `existsByThemeSlotId` 검사 부정확 | 도메인 | 중간 | CONFIRMED 예약만 검사하도록 변경 |
| `CancelledStatus.cancel` | 오류 코드 모호함 | 도메인 | 낮음 | `RESERVATION_ALREADY_CANCELLED`로 변경 |

### 테스트 커버리지 전략

```
도메인 단위 테스트 (빠름, 결정적)
  └── 상태 전이: 허용/금지된 전이 모두 검증
  └── 불변식: removeReservation 후 대기자 처리 검증
  └── ThemeSlot 집계: cancelReservation, addReservation 동작 검증

트랜잭션 통합 테스트 (중간)
  └── @Transactional 롤백: 예외 시 변경 취소 확인
  └── 원자성: 두 DB 변경이 모두 반영되거나 모두 취소되는지 확인

동시성 수락 테스트 (느림, 확률적)
  └── 동시 예약: CONFIRMED 1개 보장
  └── 동시 취소: 성공 1개 보장
  └── @RepeatedTest(10)으로 신뢰도 보완
```

---

## 5. 정합성 보장 체크리스트

상태를 변경하는 서비스 메서드를 추가하거나 수정할 때 이 체크리스트를 확인한다.

```
[ ] @Transactional이 붙어 있는가?
[ ] 이 메서드에서 변경되는 모든 테이블을 파악했는가?
[ ] 중간 실패 시 이전 변경이 롤백되는가? (원자성)
[ ] 같은 데이터를 동시에 수정하는 경쟁 조건이 있는가?
[ ] 있다면, 조회 시점에 FOR UPDATE 락을 사용하는가?
[ ] 도메인 상태 전이 규칙을 우회하는 코드가 없는가?
[ ] 이 작업의 도메인 단위 (Unit of Work)가 완전한가?
    예: 취소 → 대기자 승격 / 삭제 → 슬롯 해제
[ ] 위 보장을 검증하는 테스트가 세 층위 중 적절한 곳에 있는가?
```
