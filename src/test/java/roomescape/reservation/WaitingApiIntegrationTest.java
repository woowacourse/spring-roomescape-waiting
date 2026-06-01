package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiIntegrationTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("이름으로 본인 대기 목록 조회 API를 테스트합니다.")
    @Test
    void find_my_waitings() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertWaiting("카야", LocalDate.of(2028, 5, 6), themeId, firstTimeId);
        testHelper.insertWaiting("스타크", LocalDate.of(2028, 5, 6), themeId, firstTimeId);
        testHelper.insertWaiting("스타크", LocalDate.of(2028, 5, 7), themeId, secondTimeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", equalTo("스타크"))
                .body("[0].date", equalTo("2028-05-06"))
                .body("[0].theme.id", equalTo(themeId.intValue()))
                .body("[0].theme.name", equalTo("theme name"))
                .body("[0].time.id", equalTo(firstTimeId.intValue()))
                .body("[0].time.startAt", equalTo("09:00"))
                .body("[0].order", equalTo(2))
                .body("[1].name", equalTo("스타크"))
                .body("[1].date", equalTo("2028-05-07"))
                .body("[1].time.id", equalTo(secondTimeId.intValue()))
                .body("[1].time.startAt", equalTo("10:00"))
                .body("[1].order", equalTo(1));
    }

    @DisplayName("본인 대기 취소 API를 테스트합니다.")
    @Test
    void cancel_my_waiting() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long waitingId = testHelper.insertWaiting("스타크", LocalDate.of(2028, 5, 6), themeId, timeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().delete("/waitings/{id}", waitingId)
                .then().log().all()
                .statusCode(204);

        Integer savedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?",
                Integer.class,
                waitingId
        );

        assertThat(savedCount).isZero();
    }

    @DisplayName("동일한 대기 요청이 동시에 들어오면 1건만 성공하고 나머지는 409를 반환한다.")
    @Test
    void save_waiting_concurrently() throws Exception {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation("카야", LocalDate.of(2028, 5, 18), themeId, timeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-18");
        params.put("themeId", themeId);
        params.put("timeId", timeId);

        int requestCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        List<Integer> statusCodes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    int statusCode = RestAssured.given()
                            .contentType(ContentType.JSON)
                            .body(params)
                            .when()
                            .post("/reservations")
                            .then()
                            .extract()
                            .statusCode();

                    statusCodes.add(statusCode);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        long successCount = statusCodes.stream()
                .filter(code -> code == 201)
                .count();

        long conflictCount = statusCodes.stream()
                .filter(code -> code == 409)
                .count();

        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(requestCount - 1);

        Integer savedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE name = ? AND date = ? AND theme_id = ? AND time_id = ?",
                Integer.class,
                "스타크",
                LocalDate.of(2028, 5, 18),
                themeId,
                timeId
        );

        assertThat(savedCount).isEqualTo(1);
    }
}
