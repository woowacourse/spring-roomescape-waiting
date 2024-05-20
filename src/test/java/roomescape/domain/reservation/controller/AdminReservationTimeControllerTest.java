package roomescape.domain.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ControllerTest;
import roomescape.domain.reservation.dto.ReservationTimeAddRequest;

class AdminReservationTimeControllerTest extends ControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("insert into reservation_time (start_at) values('10:00')");
    }

    @AfterEach
    void setDown() {
        jdbcTemplate.update("delete from reservation_time");
    }

    @DisplayName("예약 시간을 추가 성공할 시, 201 created를 응답한다.")
    @Test
    void should_add_reservation_time_to_db() {
        ReservationTimeAddRequest reservationTimeAddRequest = new ReservationTimeAddRequest(LocalTime.of(11, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTimeAddRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("존재하는 예약 시간을 조회시, 200 ok를 응답한다.")
    @Test
    void should_get_reservation_time_list_in_db() {
        ReservationTimeAddRequest reservationTimeAddRequest = new ReservationTimeAddRequest(LocalTime.of(10, 0));

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("존재하는 리소스에 대한 삭제 요청시, 204 no content를 응답한다.")
    @Test
    void should_remove_reservation_time_in_db() {
        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("delete 요청 시 id값이 존재하지 않으면 500 Internel Server Error로 응답한다.")
    @Test
    void should_response_bad_request_when_nonExist_id() {
        RestAssured.given().log().all()
                .when().delete("/times/2")
                .then().log().all()
                .statusCode(400);
    }
}
