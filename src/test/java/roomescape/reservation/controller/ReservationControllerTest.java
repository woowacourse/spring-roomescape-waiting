package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.controller.dto.response.ReservationOptionResponse;
import roomescape.reservation.controller.dto.response.ReservationsAndWaitingsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @Sql(scripts = {
            "/clear.sql",
            "/popular-themes-test-data.sql"
    })
    @DisplayName("전체 날짜와 테마 조회")
    void findAllDatesAndThemes() {
        ReservationOptionResponse responses = RestAssured.given().log().all()
                .when().get("/reservations/date-and-theme")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getObject(".", ReservationOptionResponse.class);

        // 기간 검증
        final LocalDate expectedStartDate = LocalDate.now();
        final LocalDate expectedEndDate = expectedStartDate.plusDays(14 - 1);

        final List<LocalDate> actualDates = responses.dates();
        assertThat(actualDates).hasSize(14);

        assertThat(actualDates).doesNotContainAnyElementsOf(
                List.of(
                        expectedStartDate.minusDays(1),
                        expectedEndDate.plusDays(1)
                )
        );

        // 테마 검증
        assertThat(responses.themes()).hasSize(12);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("날짜와 테마를 선택해 예약가능한 시간 조회")
    void findAvailableTimesByDateAndTheme() {
        // 시간 추가
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "12:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "13:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "14:00");

        // 테마 추가
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);

        // 예약 전
        List<ReservationTimesWithStatus> timeStatusesBeforeReservation = getReservationTimeStatusResponses();
        assertThat(timeStatusesBeforeReservation.size()).isEqualTo(5);
        assertThat(countReservableTimes(timeStatusesBeforeReservation)).isEqualTo(5);

        // 예약 추가 1
        insertReservation("브라운", "2026-05-05", 1L, 1L);

        // 예약 후
        List<ReservationTimesWithStatus> timeStatusesAfterReservation = getReservationTimeStatusResponses();
        assertThat(timeStatusesAfterReservation.size()).isEqualTo(5);
        assertThat(countReservableTimes(timeStatusesAfterReservation)).isEqualTo(4);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 가능한 시간 조회시 날짜 형식이 잘못되면 400을 응답한다")
    void respondBadRequestWhenDateFormatIsInvalidForAvailableTimes() {
        RestAssured.given().log().all()
                .when().get("/reservations/available-times?date=invalid-date&themeId=1")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("잘못된 요청입니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 가능한 시간 조회시 테마 id가 없으면 400을 응답한다")
    void respondBadRequestWhenThemeIdIsMissingForAvailableTimes() {
        RestAssured.given().log().all()
                .when().get("/reservations/available-times?date=2026-05-05")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("잘못된 요청입니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약자 이름으로 예약 및 대기 목록을 조회한다")
    void findReservationsAndWaitingsByCustomerName() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);
        insertReservation("초코칩", futureDate, 1L, 1L);
        insertReservation("재키", futureDate, 2L, 1L);
        insertWaiting("초코칩", futureDate, 2L, 1L);

        ReservationsAndWaitingsResponse responses = RestAssured.given().log().all()
                .queryParam("customer-name", "초코칩")
                .queryParam("customer-email", emailFromName("초코칩"))
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getObject(".", ReservationsAndWaitingsResponse.class);

        assertThat(responses.reservations()).hasSize(1);
        assertThat(responses.reservations().getFirst().name()).isEqualTo("초코칩");
        assertThat(responses.waitings()).hasSize(1);
        assertThat(responses.waitings().getFirst().customerName()).isEqualTo("초코칩");
        assertThat(responses.waitings().getFirst().rank()).isEqualTo(1);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약을 추가하고 취소한다")
    void createAndCancelReservation() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "email", emailFromName("브라운"),
                        "date", futureDate,
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .queryParam("customer-name", "브라운")
                .queryParam("customer-email", emailFromName("브라운"))
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약을 취소할 수 없다")
    void customerCannotCancelReservationOnReservationDateBeforeStartTime() {
        final String today = LocalDate.now().toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", today, 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("customer-name", "브라운")
                .queryParam("customer-email", emailFromName("브라운"))
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("당일 예약은 취소할 수 없습니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 예약 시간으로 예약하면 404를 응답한다")
    void respondNotFoundWhenCreatingReservationWithNonExistingReservationTime() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "email", emailFromName("브라운"),
                        "date", futureDate,
                        "timeId", 999,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 시간을 선택하지 않으면 400을 응답한다")
    void respondBadRequestWhenReservationTimeIsMissing() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "email", emailFromName("브라운"),
                        "date", futureDate,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("예약 시간을 선택해야 합니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 요청 본문이 null이면 400을 응답한다")
    void respondBadRequestWhenReservationRequestBodyIsNull() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("null")
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("잘못된 요청입니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 테마로 예약하면 404를 응답한다")
    void respondNotFoundWhenCreatingReservationWithNonExistingTheme() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "email", emailFromName("브라운"),
                        "date", futureDate,
                        "timeId", 1,
                        "themeId", 999
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약자 이름이 비어있으면 400을 응답한다")
    void respondBadRequestWhenCustomerNameIsBlank() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "",
                        "email", "brown@example.com",
                        "date", futureDate,
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    private static List<ReservationTimesWithStatus> getReservationTimeStatusResponses() {
        return RestAssured.given().log().all()
                .when().get("/reservations/available-times?date=2026-05-05&themeId=1")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationTimesWithStatus.class);
    }

    private static int countReservableTimes(final List<ReservationTimesWithStatus> timeStatuses) {
        int count = 0;
        for (final ReservationTimesWithStatus timeStatus : timeStatuses) {
            if (!timeStatus.reserved()) {
                count++;
            }
        }
        return count;
    }

    private void insertReservation(final String name, final String date, final long timeId, final long themeId) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation (customer_name, customer_email, slot_id, status) VALUES (?, ?, ?, ?)",
                name,
                emailFromName(name),
                slotId,
                "CONFIRMED"
        );
    }

    private void insertWaiting(final String name, final String date, final long timeId, final long themeId) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO waiting (customer_name, customer_email, slot_id) VALUES (?, ?, ?)",
                name,
                emailFromName(name),
                slotId
        );
    }

    private String emailFromName(final String name) {
        return "customer" + Math.abs(name.hashCode()) + "@example.com";
    }

    private Long insertReservationSlot(final String date, final long timeId, final long themeId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)",
                    date,
                    timeId,
                    themeId
            );
        } catch (DuplicateKeyException ignored) {
        }
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE reservation_date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                timeId,
                themeId
        );
    }
}
