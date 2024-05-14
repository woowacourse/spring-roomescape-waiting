package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.web.controller.request.ReservationTimeWebRequest;

class ReservationTimeController extends ControllerTest {


    @BeforeEach
    void setInitialData() {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "15:00");
    }

    @DisplayName("예약 시간을 저장한다 -> 201")
    @Test
    void create() {
        ReservationTimeWebRequest request = new ReservationTimeWebRequest("12:00");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/times")
            .then().log().all()
            .statusCode(201)
            .body("id", is(3));

    }

    @DisplayName("예약 시간을 삭제한다 -> 204")
    @Test
    void deleteBy() {
        RestAssured.given().log().all()
            .when().delete("/times/1")
            .then().log().all()
            .statusCode(204);
    }

    @DisplayName("예약 시간을 조회한다. -> 200")
    @Test
    void getReservationTimes() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/times")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(2));

    }

    @DisplayName("예약 시간 포맷이 잘못되거나, 중복 될 경우 -> 400")
    @Test
    void create_IllegalTimeFormat() {
        ReservationTimeWebRequest request = new ReservationTimeWebRequest("24:00");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/times")
            .then().log().all()
            .statusCode(400);

    }

    @DisplayName("예약이 존재하는 시간 삭제 -> 400")
    @Test
    void delete_ReservationExists() {
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?)", "방탈출1", "설명1",
            "https://url1");
        jdbcTemplate.update("INSERT INTO member(name,email,password,role) VALUES (?,?,?,?)", "wiib", "asd@naver.com",
            "123asd", "ADMIN");
        jdbcTemplate.update("INSERT INTO reservation(date,time_id,theme_id,member_id) VALUES (?,?,?,?)",
            "2026-02-01", 1L, 1L, 1L);

        RestAssured.given().log().all()
            .when().delete("/times/1")
            .then().log().all()
            .statusCode(400);

    }

    @DisplayName("요청 포맷이 잘못될 경우 -> 400")
    @Test
    void create_MethodArgNotValid() {
        ReservationTimeWebRequest request = new ReservationTimeWebRequest(null);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/times")
            .then().log().all()
            .statusCode(400);

    }
}
