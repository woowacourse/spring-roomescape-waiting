package roomescape.accaptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Sql(scripts = "/acceptance-reset-mysql.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ConcurrencyWithMySqlTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("roomescape_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // application.properties의 H2 설정을 비활성화
        registry.add("spring.h2.console.enabled", () -> "false");
        // schema.sql을 MySQL 컨테이너에서도 실행
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @RepeatedTest(5)
    @DisplayName("같은 빈 슬롯에 동시에 예약하면 정확히 하나만 CONFIRMED가 된다 (MySQL)")
    void 동시에_같은_슬롯에_예약하면_하나만_CONFIRMED가_된다() throws Exception {
        // given - 미래의 빈 슬롯 ID 조회
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

        // then - CONFIRMED는 정확히 하나여야 한다
        long confirmedCount = statuses.stream()
                .filter("CONFIRMED"::equals)
                .count();
        assertThat(confirmedCount).isEqualTo(1);
    }
}
