package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.service.dto.result.AvailableDateResult;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.dto.result.ReservationTimeStatusResult;
import roomescape.support.SpringBootApiTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApiTest
class ReservationControllerTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final String STRING_TOMORROW = TOMORROW.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void 전체_날짜_조회() {
        AvailableDateResult responses = RestAssured.given().log().all()
                .when().get("/reservations/available-dates")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getObject(".", AvailableDateResult.class);

        final LocalDate expectedStartDate = TODAY;
        final LocalDate expectedEndDate = expectedStartDate.plusDays(14 - 1);

        final List<LocalDate> actualDates = responses.dates();

        assertThat(actualDates).hasSize(14).doesNotContainAnyElementsOf(
                List.of(
                        expectedStartDate.minusDays(1),
                        expectedEndDate.plusDays(1)
                )
        );
    }

    @Test
    void 날짜와_테마를_선택해_예약가능한_시간_조회() {
        // 시간 추가
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "12:00", "12:30");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "13:00", "13:30");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "14:00", "14:30");

        // 테마 추가
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        // 예약 전
        List<ReservationTimeStatusResult> timeStatusesBeforeReservation = getReservationTimeStatusResponses();
        assertThat(timeStatusesBeforeReservation).hasSize(5);
        assertThat(countReservableTimes(timeStatusesBeforeReservation)).isEqualTo(5);

        // 예약 추가
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");

        // 예약 후
        List<ReservationTimeStatusResult> timeStatusesAfterReservation = getReservationTimeStatusResponses();
        assertThat(timeStatusesAfterReservation).hasSize(5);
        assertThat(countReservableTimes(timeStatusesAfterReservation)).isEqualTo(4);
    }

    @Test
    void 잘못된_형식의_날짜로_예약가능한_시간을_조회하면_실패한다() {
        RestAssured.given().log().all()
                .when().get("/reservations/available-times?date=2024-1-1&themeId=1")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 사용자가_자신의_이름으로_본인의_예약목록_조회() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");

        List<ReservationResult> reservations = RestAssured.given().log().all()
                .when().get("/reservations?name=브라운")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResult.class);

        assertThat(reservations).hasSize(1);
        ReservationResult response = reservations.getFirst();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(response.date()).isEqualTo(TOMORROW);
        assertThat(response.time().id()).isEqualTo(1);
        assertThat(response.theme().id()).isEqualTo(1);
    }

    @Test
    void 이름_없이_예약을_조회하면_실패한다() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_추가() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", STRING_TOMORROW);
        params.put("timeId", 1);
        params.put("themeId", 1);

        ReservationResult response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201).extract()
                .jsonPath().getObject(".", ReservationResult.class);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(response.date()).isEqualTo(TOMORROW);
        assertThat(response.time().id()).isEqualTo(1);
        assertThat(response.theme().id()).isEqualTo(1);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 필수값인_이름이_누락되면_예약에_실패한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");

        Map<String, Object> params = new HashMap<>();
        params.put("date", STRING_TOMORROW);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 사용자가_본인_예약의_날짜와_시간을_변경() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", STRING_TOMORROW);
        params.put("timeId", 2);

        ReservationResult reservation = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getObject(".", ReservationResult.class);

        assertThat(reservation.id()).isEqualTo(1);
        assertThat(reservation.name()).isEqualTo("브라운");
        assertThat(reservation.date()).isEqualTo(TOMORROW);
        assertThat(reservation.time().id()).isEqualTo(2);
        assertThat(reservation.theme().id()).isEqualTo(1);
    }

    @Test
    void 다른_사람의_예약을_변경하면_실패한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "재즈");
        params.put("date", STRING_TOMORROW);
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 사용자가_본인의_예약을_취소() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    void 존재하지_않는_예약을_취소하면_실패한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().delete("/reservations/999")
                .then().log().all()
                .statusCode(404);
    }

    private static List<ReservationTimeStatusResult> getReservationTimeStatusResponses() {
        return RestAssured.given().log().all()
                .when().get("/reservations/available-times?date=" + STRING_TOMORROW + "&themeId=1")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationTimeStatusResult.class);
    }

    private static int countReservableTimes(final List<ReservationTimeStatusResult> timeStatuses) {
        int count = 0;
        for (final ReservationTimeStatusResult timeStatus : timeStatuses) {
            if (!timeStatus.reserved()) {
                count++;
            }
        }
        return count;
    }
}
