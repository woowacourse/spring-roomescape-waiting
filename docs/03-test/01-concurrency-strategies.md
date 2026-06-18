# 동시성 테스트 작성 전략

---

## 결론: CountDownLatch 방식은 표준이지만 한계가 있다. 목적에 따라 방법을 달리 써야 한다

현재 코드는 `CountDownLatch + ExecutorService` 조합으로 "거의 동시에" 출발시키는 방식이다.
이 방식은 가장 널리 쓰이는 표준이지만 "진짜 동시"를 보장하지 않고, 경쟁 조건을 확률적으로만 유발한다.
목적(버그 발견 / 부하 / 락 검증)에 따라 더 적합한 도구가 존재한다.

---

## 1. 현재 방식 — CountDownLatch + ExecutorService

### 동작 원리

```
스레드1 ───────────── await(startLatch) ──┐
스레드2 ───────────── await(startLatch) ──┤── countDown(startLatch=0) ──→ 동시 출발
스레드3 ───────────── await(startLatch) ──┘
          ↑ 여기까지는 모두 준비 완료 상태로 대기
```

```java
CountDownLatch startLatch = new CountDownLatch(1);   // 출발 신호탄
CountDownLatch doneLatch  = new CountDownLatch(threadCount); // 완료 대기

// 모든 스레드가 startLatch.await()에서 블록된 상태로 준비
for (int i = 0; i < threadCount; i++) {
    executor.submit(() -> {
        startLatch.await(); // ← 신호탄 터지기 전까지 여기서 대기
        // HTTP 요청
        doneLatch.countDown();
    });
}

startLatch.countDown(); // ← 신호탄 → 모든 스레드 동시 출발
doneLatch.await(10, TimeUnit.SECONDS); // ← 모두 끝날 때까지 대기
```

### 장점

- 구현이 단순하고 직관적이다
- 자바 표준 라이브러리만 사용한다
- HTTP 레벨에서 실제 요청을 보내므로 전체 스택(Controller → Service → DB)을 통과한다

### 한계

| 한계 | 원인 |
|---|---|
| 진짜 동시를 보장하지 않음 | JVM 스케줄러가 스레드 실행 순서를 결정 |
| 플리키(flaky) 테스트 | 경쟁 조건이 발생하지 않으면 우연히 통과 |
| 재현율이 낮음 | 스레드 수가 적으면 경쟁이 안 일어날 수 있음 |
| `startLatch.await()` 이후에도 OS 스케줄링으로 차이 발생 | 나노초 단위 오차 존재 |

```java
// 현재 코드의 실제 흐름 — "거의 동시"이지 진짜 동시가 아니다
startLatch.countDown();
// 스레드1: HTTP 요청 시작 (t=0ms)
// 스레드2: HTTP 요청 시작 (t=0.03ms) ← JVM 스케줄링으로 약간 늦음
// 스레드3: HTTP 요청 시작 (t=0.05ms)
// → DB에서 경쟁 조건이 발생할 수도, 안 할 수도 있다
```

---

## 2. CyclicBarrier — 더 정확한 동시 출발

### 차이점

`CountDownLatch`가 "하나가 신호를 보내면 나머지가 출발"이라면,
`CyclicBarrier`는 "모든 스레드가 준비됐을 때 동시에 출발"이다.
또한 재사용이 가능하고, 배리어 지점에서 콜백을 실행할 수 있다.

```java
int threadCount = 5;
CyclicBarrier barrier = new CyclicBarrier(threadCount, () -> {
    // 모든 스레드가 배리어에 도달하면 이 콜백이 한 번 실행됨
    System.out.println("모든 스레드 준비 완료 — 동시 출발!");
});

List<String> statuses = new CopyOnWriteArrayList<>();
ExecutorService executor = Executors.newFixedThreadPool(threadCount);

for (int i = 0; i < threadCount; i++) {
    String name = "사용자" + i;
    executor.submit(() -> {
        try {
            barrier.await(); // ← 모든 스레드가 여기 도달할 때까지 대기
            // 모든 스레드가 동시에 이 줄부터 실행
            String status = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", name, "themeSlotId", themeSlotId))
                    .when().post("/reservations")
                    .then().extract().jsonPath().getString("status");
            statuses.add(status);
        } catch (Exception ignored) {}
    });
}

executor.shutdown();
executor.awaitTermination(10, TimeUnit.SECONDS);

assertThat(statuses.stream().filter("CONFIRMED"::equals).count()).isEqualTo(1);
```

### CountDownLatch vs CyclicBarrier 비교

| | CountDownLatch | CyclicBarrier |
|---|---|---|
| 출발 방식 | 하나가 신호를 보냄 (1 → 0) | 모두가 준비되면 자동 출발 |
| 재사용 | 불가 | 가능 (`reset()`) |
| 동시성 정확도 | 낮음 (출발 순서 차이 있음) | 높음 (더 균등한 출발) |
| 배리어 콜백 | 없음 | 있음 |

---

## 3. CompletableFuture — 비동기 체이닝 방식

### 특징

스레드를 직접 관리하지 않고 비동기 작업을 체이닝하는 방식이다.
코드가 선언적이고 에러 처리가 명확하다.

```java
long themeSlotId = 새_슬롯_조회(1);
int threadCount = 5;

// 모든 작업을 CompletableFuture로 만들고 한꺼번에 실행
List<CompletableFuture<String>> futures = IntStream.range(0, threadCount)
        .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
            try {
                return RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(Map.of("name", "사용자" + i, "themeSlotId", themeSlotId))
                        .when().post("/reservations")
                        .then().extract().jsonPath().getString("status");
            } catch (Exception e) {
                return "ERROR";
            }
        }))
        .toList();

// 모든 Future가 완료될 때까지 대기
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

List<String> statuses = futures.stream()
        .map(CompletableFuture::join)
        .toList();

assertThat(statuses.stream().filter("CONFIRMED"::equals).count()).isEqualTo(1);
```

### 장점 / 단점

- 장점: 코드가 간결, 에러 핸들링 명확, 타임아웃 설정 쉬움
- 단점: 동시 출발을 강제하지 않음 (CountDownLatch보다 동시성 낮음)

---

## 4. @RepeatedTest — 플리키 테스트의 신뢰도 높이기

동시성 버그는 "가끔만" 발생한다. 한 번 실행으로는 발견 못 할 수 있다.
`@RepeatedTest`로 여러 번 반복하면 발견 확률이 높아진다.

```java
@RepeatedTest(10)  // 10번 반복 실행
@DisplayName("같은 슬롯에 동시에 예약하면 CONFIRMED는 하나여야 한다 (반복)")
void 동시_예약_반복_검증(RepetitionInfo repetitionInfo) throws InterruptedException {
    System.out.println("실행 횟수: " + repetitionInfo.getCurrentRepetition());

    long themeSlotId = 새_슬롯_조회(1);
    // ... CountDownLatch 방식 동일

    assertThat(confirmedCount).isEqualTo(1);
}
```

### 주의

`@RepeatedTest`를 쓰면 매번 DB 상태가 달라진다.
`@Sql`의 `executionPhase = BEFORE_TEST_METHOD`가 있으면 각 반복마다 초기화된다.

---

## 5. Semaphore — 동시 실행 수를 정밀 제어

특정 시점에 정확히 N개의 스레드가 동시에 접근하게 만들 수 있다.

```java
int threadCount = 5;
Semaphore semaphore = new Semaphore(0); // 초기값 0 → 모두 대기
List<String> statuses = new CopyOnWriteArrayList<>();
List<Thread> threads = new ArrayList<>();

for (int i = 0; i < threadCount; i++) {
    String name = "사용자" + i;
    Thread thread = new Thread(() -> {
        try {
            semaphore.acquire(); // ← 퍼밋이 0이므로 여기서 대기
            String status = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", name, "themeSlotId", themeSlotId))
                    .when().post("/reservations")
                    .then().extract().jsonPath().getString("status");
            statuses.add(status);
        } catch (InterruptedException ignored) {}
    });
    threads.add(thread);
    thread.start();
}

// 모든 스레드가 acquire()에서 대기한 뒤 한꺼번에 퍼밋 발행
semaphore.release(threadCount); // ← 5개 동시 출발

for (Thread thread : threads) {
    thread.join();
}

assertThat(statuses.stream().filter("CONFIRMED"::equals).count()).isEqualTo(1);
```

---

## 6. 가장 효과적인 조합 — Testcontainers + CyclicBarrier

**현재 H2를 쓰면 안 되는 이유**: H2와 MySQL의 락 모델이 다르다. H2에서 `FOR UPDATE`가 통과해도 MySQL에서 깨질 수 있다. 락 동작을 검증하는 동시성 테스트는 반드시 실제 DB를 써야 한다.

Testcontainers를 쓰면 테스트 시 실제 MySQL 컨테이너를 띄워서 실제 락 동작을 검증할 수 있다.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ConcurrencyWithMySQLTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("roomescape_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void 동시에_같은_슬롯에_예약하면_하나만_CONFIRMED가_된다() throws InterruptedException {
        int threadCount = 5;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        List<String> statuses = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            String name = "사용자" + i;
            executor.submit(() -> {
                try {
                    barrier.await(); // 모든 스레드 준비 후 동시 출발
                    String status = RestAssured.given()...;
                    statuses.add(status);
                } catch (Exception ignored) {}
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(statuses.stream().filter("CONFIRMED"::equals).count()).isEqualTo(1);
    }
}
```

---

## 방법 비교 요약

| 방법 | 동시성 정확도 | 구현 복잡도 | 재현율 | 언제 쓰나 |
|---|---|---|---|---|
| `CountDownLatch` | 낮음 (확률적) | 낮음 | 낮음 | 기본적인 동시성 검증 시작점 |
| `CyclicBarrier` | 중간 (더 균등) | 낮음 | 중간 | 더 균등한 동시 출발이 필요할 때 |
| `CompletableFuture` | 낮음 | 낮음 | 낮음 | 코드 간결함 우선, 동시성 낮아도 될 때 |
| `@RepeatedTest` | - (횟수로 보완) | 낮음 | 높음 | 플리키 테스트 신뢰도를 높일 때 |
| `Semaphore` | 높음 | 중간 | 중간 | 정확한 동시 실행 수 제어가 필요할 때 |
| Testcontainers + CyclicBarrier | 높음 | 높음 | 높음 | 락 동작 자체를 검증할 때 (권장) |

---

## 현재 프로젝트에서 권장하는 순서

```
1. 지금처럼 CountDownLatch로 먼저 버그 존재 증명 (현재 진행 중)
2. @RepeatedTest(10) 추가로 신뢰도 높이기
3. FOR UPDATE 락 적용
4. 테스트 통과 확인
5. (여유가 생기면) Testcontainers로 MySQL 환경에서 재검증
```

단계별로 진행하면 "버그 발견 → 증명 → 수정 → 검증"의 흐름이 명확해진다.
