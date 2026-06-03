package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
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

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(
                    LocalDate.of(2026, 5, 1)
                            .atStartOfDay(ZoneId.of("Asia/Seoul"))
                            .toInstant(),
                    ZoneId.of("Asia/Seoul")
            );
        }
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
        final LocalDate expectedStartDate = LocalDate.of(2026, 05, 01);
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
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

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
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        insertReservation("초코칩", "2026-05-13", 1L, 1L);
        insertReservation("재키", "2026-05-13", 2L, 1L);
        insertWaiting("초코칩", "2026-05-13", 2L, 1L);

        ReservationsAndWaitingsResponse responses = RestAssured.given().log().all()
                .queryParam("customer-name", "초코칩")
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
    @DisplayName("예약을 추가하고 삭제한다")
    void createAndDeleteReservation() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-08-05",
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 일정을 수정한다")
    void updateReservationSchedule() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-06",
                        "timeId", 2
                ))
                .when().put("/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.is(1))
                .body("name", org.hamcrest.Matchers.is("브라운"))
                .body("date", org.hamcrest.Matchers.is("2026-08-06"))
                .body("time.id", org.hamcrest.Matchers.is(2))
                .body("theme.id", org.hamcrest.Matchers.is(1));

        Map<String, Object> updatedReservation = jdbcTemplate.queryForMap(
                """
                        SELECT s.reservation_date, s.time_id
                        FROM reservation r
                        JOIN reservation_slot s ON r.slot_id = s.id
                        WHERE r.id = ?
                        """,
                1L
        );
        assertThat(updatedReservation.get("RESERVATION_DATE").toString()).isEqualTo("2026-08-06");
        assertThat(updatedReservation.get("TIME_ID")).isEqualTo(2L);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 예약을 수정하면 404를 응답한다")
    void respondNotFoundWhenUpdatingNonExistingReservation() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-06",
                        "timeId", 1
                ))
                .when().put("/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.is("존재하지 않는 예약입니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 예약 시간으로 수정하면 404를 응답한다")
    void respondNotFoundWhenUpdatingWithNonExistingReservationTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-06",
                        "timeId", 999
                ))
                .when().put("/reservations/1")
                .then().log().all()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.is("존재하지 않는 예약 시간입니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 수정시 예약일을 입력하지 않으면 400을 응답한다")
    void respondBadRequestWhenReservationDateIsMissingOnUpdate() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "timeId", 1
                ))
                .when().put("/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("예약일을 입력해야 합니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("과거 시간으로 예약을 수정하면 400을 응답한다")
    void respondBadRequestWhenUpdatingReservationToPastTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-04-30",
                        "timeId", 2
                ))
                .when().put("/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("과거 시간으로는 예약할 수 없습니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("이미 예약된 시간으로 수정하면 409를 응답한다")
    void respondConflictWhenUpdatingToAlreadyReservedTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);
        insertReservation("재키", "2026-08-05", 2L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-05",
                        "timeId", 2
                ))
                .when().put("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("이미 예약된 시간입니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약을 수정할 수 없다")
    void customerCannotUpdateReservationOnReservationDateBeforeStartTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-05-01", 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-05-02",
                        "timeId", 2
                ))
                .when().put("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("당일 예약은 변경할 수 없습니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약을 취소할 수 없다")
    void customerCannotCancelReservationOnReservationDateBeforeStartTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-05-01", 1L, 1L);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("당일 예약은 취소할 수 없습니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 예약 시간으로 예약하면 404를 응답한다")
    void respondNotFoundWhenCreatingReservationWithNonExistingReservationTime() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-05-05",
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
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-05-05",
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
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-05-05",
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
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "",
                        "date", "2026-05-05",
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
                "INSERT INTO reservation (customer_name, slot_id) VALUES (?, ?)",
                name,
                slotId
        );
    }

    private void insertWaiting(final String name, final String date, final long timeId, final long themeId) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO waiting (customer_name, slot_id) VALUES (?, ?)",
                name,
                slotId
        );
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
