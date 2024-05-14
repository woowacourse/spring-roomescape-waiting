package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.Fixture.COOKIE_NAME;
import static roomescape.Fixture.VALID_USER_EMAIL;
import static roomescape.Fixture.VALID_USER_NAME;
import static roomescape.Fixture.VALID_USER_PASSWORD;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.MemberRole;
import roomescape.web.controller.request.MemberReservationWebRequest;

class ReservationControllerTest extends ControllerTest {

    @BeforeEach
    void setInitialData() {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "12:00");
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?)", "방탈출1", "설명1",
            "https://url1");
        jdbcTemplate.update("INSERT INTO member(name,email,password,role) VALUES (?,?,?,?)",
            VALID_USER_NAME.getName(), VALID_USER_EMAIL.getEmail(),
            VALID_USER_PASSWORD.getPassword(), MemberRole.USER.name());
        jdbcTemplate.update("INSERT INTO reservation(date,time_id,theme_id,member_id) VALUES (?,?,?,?)",
            "2026-02-01", 1L, 1L, 1L);
    }

    @DisplayName("예약을 저장한다. -> 201")
    @Test
    void reserve() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-01-02", 1L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("name", is(VALID_USER_NAME.getName()));
    }

    @DisplayName("예약을 삭제한다. -> 204")
    @Test
    void deleteBy() {
        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);
    }

    @DisplayName("예약을 조회한다. -> 200")
    @Test
    void getReservations() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));
    }

    @DisplayName("실패: 예약 날짜가 잘못될 경우 -> 400")
    @Test
    void reserve_IllegalDateRequest() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-00-02", 1L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("실패: 존재하지 않는 테마에 대한 예약  -> 400")
    @Test
    void reserve_NoSuchTheme() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-01-02", 1L, 200L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("실패: 존재하지 않는 예약 시간에 대한 예약  -> 400")
    @Test
    void reserve_NoSuchTime() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2040-01-02", 100L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("과거 시간에 예약을 넣을 경우 -> 400")
    @Test
    void reserve_PastTime() {
        MemberReservationWebRequest request = new MemberReservationWebRequest("2024-05-10", 100L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(COOKIE_NAME, getUserToken())
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }
}
