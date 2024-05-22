package roomescape.reservation.controller;

import static roomescape.fixture.MemberFixture.getMemberAdmin;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.service.TokenProvider;
import roomescape.util.ControllerTest;

@DisplayName("예약 페이지 테스트")
class ReservationPageControllerTest extends ControllerTest {

    @DisplayName("기본 페이지 조회에 성공한다.")
    @Test
    void mainPage() {
        //given & when & then
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("예약 페이지 조회에 성공한다.")
    @Test
    void getReservationPage() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("나의 예약 페이지 조회에 성공한다.")
    @Test
    void getMyReservationPage() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(200);
    }
}
