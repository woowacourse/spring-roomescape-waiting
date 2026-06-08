package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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
class ReservationApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiIntegrationTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("방탈출 예약 추가 API를 테스트합니다.")
    @Test
    void save_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-06");
        params.put("themeId", String.valueOf(themeId));
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .body("name", equalTo("스타크"))
                .body("date", equalTo("2028-05-06"))
                .body("time.id", equalTo(timeId.intValue()))
                .body("time.startAt", equalTo("09:00"))
                .body("theme.id", equalTo(themeId.intValue()))
                .body("theme.name", equalTo("theme name"));
    }

    @DisplayName("동일한 사용자가 이미 예약한 슬롯에는 대기할 수 없습니다.")
    @Test
    void save_waiting_fails_when_same_user_already_reserved_same_schedule() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, timeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-06");
        params.put("themeId", String.valueOf(themeId));
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);

        Integer waitingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE name = ? AND date = ? AND theme_id = ? AND time_id = ?",
                Integer.class,
                "스타크",
                LocalDate.of(2028, 5, 6),
                themeId,
                timeId
        );

        assertThat(waitingCount).isZero();
    }

    @DisplayName("이름으로 본인 예약 목록 조회 API를 테스트합니다.")
    @Test
    void find_my_reservations() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, firstTimeId);
        testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 7), themeId, secondTimeId);
        testHelper.insertReservation("카야", LocalDate.of(2028, 5, 8), themeId, secondTimeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", equalTo("스타크"))
                .body("[1].name", equalTo("스타크"));
    }

    @DisplayName("본인 예약 변경 API를 테스트합니다.")
    @Test
    void update_my_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, firstTimeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-07");
        params.put("timeId", String.valueOf(secondTimeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(reservationId.intValue()))
                .body("date", equalTo("2028-05-07"))
                .body("time.id", equalTo(secondTimeId.intValue()));
    }

    @DisplayName("예약 변경 시 기존 예약 시간의 가장 오래된 대기가 예약으로 변경됩니다.")
    @Test
    void update_my_reservation_with_waiting() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, firstTimeId);
        Long waitingId = testHelper.insertWaiting("카야", LocalDate.of(2028, 5, 6), themeId, firstTimeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-07");
        params.put("timeId", String.valueOf(secondTimeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("스타크"))
                .body("date", equalTo("2028-05-07"))
                .body("time.id", equalTo(secondTimeId.intValue()));

        String promotedReservationOwner = jdbcTemplate.queryForObject(
                """
                        SELECT name
                        FROM reservation
                        WHERE date = ? AND theme_id = ? AND time_id = ?
                        """,
                String.class,
                LocalDate.of(2028, 5, 6),
                themeId,
                firstTimeId
        );
        Integer waitingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?",
                Integer.class,
                waitingId
        );

        assertThat(promotedReservationOwner).isEqualTo("카야");
        assertThat(waitingCount).isZero();
    }

    @DisplayName("예약 변경 시 첫 번째 대기자가 예약으로 전환되면 다음 대기자의 순번이 1번이 됩니다.")
    @Test
    void update_reservation_with_waiting_reorders_remaining_waitings() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2028, 5, 6);
        Long reservationId = testHelper.insertReservation("스타크", date, themeId, firstTimeId);
        testHelper.insertWaiting("카야", date, themeId, firstTimeId);
        testHelper.insertWaiting("타스", date, themeId, firstTimeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-07");
        params.put("timeId", String.valueOf(secondTimeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(200);

        RestAssured.given()
                .queryParam("name", "타스")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("타스"))
                .body("[0].order", equalTo(1));
    }

    @DisplayName("본인 예약 취소 API를 테스트합니다.")
    @Test
    void cancel_my_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, timeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예약 취소 시 첫 번째 대기자가 예약자로 전환됩니다.")
    @Test
    void cancel_reservation_promotes_first_waiting() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2028, 5, 6);
        Long reservationId = testHelper.insertReservation("스타크", date, themeId, timeId);
        Long firstWaitingId = testHelper.insertWaiting("카야", date, themeId, timeId);
        Long secondWaitingId = testHelper.insertWaiting("타스", date, themeId, timeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(204);

        Integer deletedReservationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class,
                reservationId
        );
        String promotedReservationOwner = jdbcTemplate.queryForObject(
                """
                        SELECT name
                        FROM reservation
                        WHERE date = ? AND theme_id = ? AND time_id = ?
                        """,
                String.class,
                date,
                themeId,
                timeId
        );
        Integer firstWaitingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?",
                Integer.class,
                firstWaitingId
        );
        Integer secondWaitingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?",
                Integer.class,
                secondWaitingId
        );

        assertThat(deletedReservationCount).isZero();
        assertThat(promotedReservationOwner).isEqualTo("카야");
        assertThat(firstWaitingCount).isZero();
        assertThat(secondWaitingCount).isEqualTo(1);
    }

    @DisplayName("첫 번째 대기자가 예약으로 전환되면 다음 대기자의 순번이 1번이 됩니다.")
    @Test
    void promotion_reorders_remaining_waitings() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2028, 5, 6);
        Long reservationId = testHelper.insertReservation("스타크", date, themeId, timeId);
        testHelper.insertWaiting("카야", date, themeId, timeId);
        testHelper.insertWaiting("타스", date, themeId, timeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .queryParam("name", "타스")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("타스"))
                .body("[0].order", equalTo(1));
    }

    @DisplayName("동일한 예약 요청이 동시에 들어오면 1건만 성공하고 나머지는 409를 반환한다.")
    @Test
    void save_reservation_concurrently() throws Exception {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("name", "카야");
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
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND theme_id = ? AND time_id = ?",
                Integer.class,
                LocalDate.of(2028, 5, 18),
                themeId,
                timeId
        );

        assertThat(savedCount).isEqualTo(1);
    }
}
