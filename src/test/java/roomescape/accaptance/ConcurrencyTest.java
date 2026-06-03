package roomescape.accaptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservationStatus.ConfirmedStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/acceptance-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ConcurrencyTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @RepeatedTest(10)
    @DisplayName("같은 빈 슬롯에 동시에 예약하면 정확히 하나만 CONFIRMED가 된다.")
    void 동시에_같은_슬록에_예약하면_하나만_CONFIRMED가_된다() throws InterruptedException {

        //given
        long themeSlotId = RestAssured.given()
                .when().get("/times?themeId=1&date=" + LocalDate.now().plusMonths(6))
                .then().statusCode(200)
                .extract().jsonPath().getLong("[0].id");

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<String> statuses = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            String name = "사용자" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
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

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        long confirmedCount = statuses.stream()
                .filter(ConfirmedStatus.getInstance().getName()::equals)
                .count();
        assertThat(confirmedCount).isEqualTo(1);
    }
}
