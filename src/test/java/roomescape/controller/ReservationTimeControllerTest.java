package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.service.dto.command.ReservationTimeCreateCommand;
import roomescape.service.dto.result.ReservationTimeResult;
import roomescape.support.SpringBootApiTest;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApiTest
class ReservationTimeControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void 전체_예약시간_조회() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");

        List<ReservationTimeResult> reservationTimes = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationTimeResult.class);

        assertThat(reservationTimes).hasSize(2);

        ReservationTimeResult reservationTime1 = reservationTimes.getFirst();
        assertThat(reservationTime1.id()).isEqualTo(1);
        assertThat(reservationTime1.startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservationTime1.endAt()).isEqualTo(LocalTime.of(10, 30));

        ReservationTimeResult reservationTime2 = reservationTimes.get(1);
        assertThat(reservationTime2.id()).isEqualTo(2);
        assertThat(reservationTime2.startAt()).isEqualTo(LocalTime.of(11, 0));
        assertThat(reservationTime2.endAt()).isEqualTo(LocalTime.of(11, 30));
    }

    @Test
    void 예약시간_생성() {
        LocalTime startAt = LocalTime.of(12, 0);
        LocalTime endAt = LocalTime.of(12, 30);
        ReservationTimeCreateCommand request = new ReservationTimeCreateCommand(
                startAt, endAt
        );

        ReservationTimeResult response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/times/1")
                .extract().as(ReservationTimeResult.class);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.startAt()).isEqualTo(startAt);
        assertThat(response.endAt()).isEqualTo(endAt);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM reservation_time", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 시작_시간이_누락되면_예약시간_생성에_실패한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("endAt", "12:30");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약시간_삭제() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM reservation_time", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    void 존재하지_않는_예약시간을_삭제하면_실패한다() {
        RestAssured.given().log().all()
                .when().delete("/times/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약이_존재하는_예약시간을_삭제하면_실패한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2024-01-01", "1", "1");

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(409);
    }
}
