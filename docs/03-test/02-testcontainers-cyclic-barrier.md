# Testcontainers + CyclicBarrier 동시성 테스트

---

## 왜 이 조합인가

### H2는 MySQL의 락 동작을 재현하지 못한다

현재 테스트는 H2 인메모리 DB 위에서 돌아간다.
`FOR UPDATE` 쿼리를 H2에서 실행하면 구문 오류 없이 통과하지만,
H2와 MySQL의 행 잠금 모델이 달라서 MySQL에서 발생하는 경쟁 조건을 H2가 재현하지 못할 수 있다.

Testcontainers는 테스트 실행 시 실제 MySQL Docker 컨테이너를 띄워서
운영 환경과 동일한 락 동작을 테스트 안에서 검증할 수 있게 해준다.

### CountDownLatch보다 CyclicBarrier가 더 균등하게 동시에 출발한다

```
CountDownLatch(1) — 메인 스레드가 신호를 보내는 방식

  메인   ─────────────────── countDown() ──┐
  스레드1 ──await(startLatch) ──────────────┤ 출발
  스레드2 ──await(startLatch) ──────────────┤ 출발
  스레드3 ──await(startLatch) ──────────────┘ 출발
         ↑ 스레드가 준비됐는지와 무관하게 메인이 신호를 보냄


CyclicBarrier(3) — 스레드 스스로 모이는 방식

  스레드1 ──await(barrier) ──┐
  스레드2 ──await(barrier) ──┤ 셋 다 도달하면 자동으로 동시 출발
  스레드3 ──await(barrier) ──┘
         ↑ 마지막 스레드가 도착하는 순간 모두 풀린다
```

CountDownLatch는 메인이 신호를 보내는 시점에 각 스레드의 준비 상태가 다를 수 있다.
CyclicBarrier는 모든 스레드가 배리어에 도달한 뒤 함께 출발하므로 출발 시점의 분산이 더 작다.

---

## 1단계: `build.gradle` 의존성 추가

```groovy
dependencies {
    // 기존 의존성 유지 ...

    // Testcontainers (Spring Boot 3.1+)
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:mysql'

    // MySQL JDBC 드라이버 (컨테이너 연결에 필요)
    testRuntimeOnly 'com.mysql:mysql-connector-j'
}
```

`spring-boot-testcontainers`는 `@DynamicPropertySource` 없이도 컨테이너를 datasource에
자동으로 연결하는 편의 기능을 제공하지만, 여기서는 명시적인 `@DynamicPropertySource`를 사용한다.
무슨 설정이 어떻게 바뀌는지 코드에서 직접 보이는 편이 이해하기 쉽기 때문이다.

---

## 2단계: 컨테이너 설정과 datasource 연결

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers  // JUnit 5용 Testcontainers 확장 — @Container 필드를 관리한다
@Sql(scripts = "/acceptance-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ConcurrencyWithMySQLTest {

    // static: 테스트 클래스 전체에서 컨테이너를 하나만 띄우고 재사용한다.
    // non-static으로 선언하면 테스트 메서드마다 컨테이너를 새로 띄우므로 매우 느려진다.
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("roomescape_test")
            .withUsername("test")
            .withPassword("test");

    // 컨테이너가 실제로 바인딩된 포트와 URL은 실행 전까지 알 수 없다.
    // @DynamicPropertySource가 애플리케이션 컨텍스트 생성 직전에 이 값을 주입한다.
    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",                mysql::getJdbcUrl);
        registry.add("spring.datasource.username",           mysql::getUsername);
        registry.add("spring.datasource.password",           mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // application.properties의 H2 설정을 비활성화
        registry.add("spring.h2.console.enabled",            () -> "false");
        // schema.sql을 MySQL 컨테이너에서도 실행
        registry.add("spring.sql.init.mode",                 () -> "always");
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }
}
```

**`@DynamicPropertySource`가 하는 일**

`application.properties`에 `spring.datasource.url=jdbc:h2:mem:database`가 있어도,
`@DynamicPropertySource`로 등록한 값이 더 높은 우선순위를 가진다.
컨텍스트가 뜰 때 MySQL 컨테이너의 JDBC URL, 계정 정보로 datasource가 구성된다.

---

## 3단계: CyclicBarrier로 동시 예약 테스트

```java
@RepeatedTest(5)
@DisplayName("같은 빈 슬롯에 동시에 예약하면 정확히 하나만 CONFIRMED가 된다 (MySQL)")
void 동시에_같은_슬롯에_예약하면_하나만_CONFIRMED가_된다() throws Exception {
    // given — 미래의 빈 슬롯 ID 조회
    long themeSlotId = RestAssured.given()
            .when().get("/times?themeId=1&date=" + LocalDate.now().plusMonths(6))
            .then().statusCode(200)
            .extract().jsonPath().getLong("[0].id");

    int threadCount = 5;
    List<String> statuses = new CopyOnWriteArrayList<>();

    // CyclicBarrier: threadCount개의 스레드가 모두 await()를 호출하면 동시에 출발
    CyclicBarrier barrier = new CyclicBarrier(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);  // 완료 대기는 latch 사용

    for (int i = 0; i < threadCount; i++) {
        String name = "사용자" + i;
        executor.submit(() -> {
            try {
                barrier.await();  // 모든 스레드가 여기 도달할 때까지 대기 → 동시 출발
                String status = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(Map.of("name", name, "themeSlotId", themeSlotId))
                        .when().post("/reservations")
                        .then().extract().jsonPath().getString("status");
                statuses.add(status);
            } catch (BrokenBarrierException | InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
                // HTTP 오류 응답(409 등)은 무시 — statuses에 추가되지 않음
            } finally {
                doneLatch.countDown();
            }
        });
    }

    doneLatch.await(15, TimeUnit.SECONDS);
    executor.shutdown();

    // then — CONFIRMED는 정확히 하나여야 한다
    long confirmedCount = statuses.stream()
            .filter("CONFIRMED"::equals)
            .count();
    assertThat(confirmedCount).isEqualTo(1);
}
```

---

## 4단계: CyclicBarrier로 동시 취소 테스트

```java
@RepeatedTest(3)
@DisplayName("같은 CONFIRMED 예약에 동시에 취소를 보내면 한 번만 성공한다 (MySQL)")
void 동시에_같은_예약을_취소하면_한_번만_처리된다() throws Exception {
    long reservationId = 1L;  // acceptance-reset.sql 기준의 CONFIRMED 예약

    int threadCount = 3;
    List<Integer> statusCodes = new CopyOnWriteArrayList<>();
    CyclicBarrier barrier = new CyclicBarrier(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                barrier.await();  // 모두 준비되면 동시 출발
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

    doneLatch.await(15, TimeUnit.SECONDS);
    executor.shutdown();

    // then — 204 No Content(성공)는 정확히 한 번이어야 한다
    long successCount = statusCodes.stream().filter(c -> c == 204).count();
    assertThat(successCount).isEqualTo(1);
}
```

---

## CyclicBarrier의 추가 기능: 배리어 콜백

모든 스레드가 배리어에 도달한 직후 딱 한 번 실행되는 콜백을 지정할 수 있다.
디버깅이나 로깅에 유용하다.

```java
CyclicBarrier barrier = new CyclicBarrier(threadCount, () -> {
    // 마지막 스레드가 await()를 호출한 직후, 모든 스레드가 풀리기 직전에 실행됨
    System.out.println("[" + LocalDateTime.now() + "] 모든 스레드 준비 완료 — 동시 출발!");
});
```

---

## CountDownLatch vs CyclicBarrier 비교

| | CountDownLatch | CyclicBarrier |
|---|---|---|
| 출발 방식 | 메인 스레드가 `countDown()`으로 신호 | 스레드 스스로 `await()`로 집결 |
| 재사용 | 불가 (0이 되면 끝) | `reset()`으로 재사용 가능 |
| 동시성 균등도 | 낮음 (메인과 워커 사이 신호 전달 지연) | 높음 (마지막 스레드가 도착하는 순간 모두 출발) |
| 배리어 콜백 | 없음 | 있음 |
| 완료 대기 | `await()`로 완료까지 대기 | 완료 대기는 별도 `CountDownLatch` 필요 |

**실무 조합**: 동시 출발은 `CyclicBarrier`, 모든 스레드 완료 대기는 `CountDownLatch`.
위 예시 코드에서 두 개를 함께 쓰는 이유가 이것이다.

---

## H2 테스트와의 관계

| | 기존 H2 테스트 | MySQL Testcontainers 테스트 |
|---|---|---|
| 목적 | 락 없이 버그 존재 증명 | 락 적용 후 실제 환경에서 검증 |
| 속도 | 빠름 | 느림 (컨테이너 시작 ~10초) |
| 신뢰도 | 낮음 (락 모델 다름) | 높음 (운영 환경과 동일) |
| CI | 항상 실행 | 시간이 걸리므로 별도 분리 가능 |

두 테스트를 모두 유지하는 것이 이상적이다.
H2 테스트는 빠른 피드백, MySQL 테스트는 최종 검증 역할을 한다.
