package roomescape.domain.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.fixture.LocalDateFixture.AFTER_THREE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.AFTER_TWO_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.TODAY;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ControllerTest;
import roomescape.domain.reservation.dto.ReservationAddRequest;

class ReservationControllerTest extends ControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "insert into member (name, email, password, role) values ('도도', 'dodo@gmail.com', '123123', 'MEMBER')");
        jdbcTemplate.update("insert into reservation_time values(1,'10:00')");
        jdbcTemplate.update("insert into theme values(1,'리비', '리비 설명', 'url')");
        jdbcTemplate.update("insert into reservation (date, time_id, theme_id, member_id) values(?,?,?,?)"
                , AFTER_TWO_DAYS_DATE, 1, 1, 1);
    }

    @AfterEach
    void setDown() {
        jdbcTemplate.update("delete from reservation");
    }

    @DisplayName("예약 목록을 불러올 수 있다.")
    @Test
    void should_response_reservation_list_when_request_reservations() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("필터링된 예약 목록을 불러올 수 있다.")
    @Test
    void should_response_filtering_reservation_list_when_request_reservations() {
        RestAssured.given().log().all()
                .queryParam("themeId", 1)
                .queryParam("memberId", 1)
                .queryParam("dateFrom", TODAY.toString())
                .queryParam("dateTo", AFTER_TWO_DAYS_DATE.toString())
                .when().get("/reservations/search")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("멤버의 예약을 추가를 성공할 시, 201 ok를 응답한다,")
    @Test
    void should_add_reservation_when_post_request_member_reservations() {
        String cookie = getMemberCookie();

        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(
                AFTER_THREE_DAYS_DATE, 1L, 1L, null);
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .contentType(ContentType.JSON)
                .body(reservationAddRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 가능 시각 목록을 불러올 수 있다. (200 OK)")
    @Test
    void should_response_bookable_time() {
        RestAssured.given().log().all()
                .when().get("/bookable-times?date=" + AFTER_TWO_DAYS_DATE + "&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("존재하는 리소스에 대한 삭제 요청시, 204 no content를 응답한다.")
    @Test
    void should_remove_reservation_when_delete_request_reservations_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 리소스에 대한 삭제 요청시, 500 Internel Server Error를 응답한다.")
    @Test
    void should_response_bad_request_when_nonExist_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/2")
                .then().log().all()
                .statusCode(400);
    }
}
