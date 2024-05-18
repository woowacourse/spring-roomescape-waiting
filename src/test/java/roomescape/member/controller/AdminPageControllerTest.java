package roomescape.member.controller;

import static roomescape.fixture.MemberFixture.getMemberAdmin;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.service.TokenProvider;
import roomescape.util.ControllerTest;

@DisplayName("관리자 페이지 테스트")
class AdminPageControllerTest extends ControllerTest {
    @Autowired
    TokenProvider tokenProvider;

    String token;

    @BeforeEach
    void setUp() {
        token = tokenProvider.createAccessToken(getMemberAdmin().getEmail());
    }

    @DisplayName("관리자 메인 페이지 조회에 성공한다.")
    @Test
    void adminMainPage() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("관리자 예약 페이지 조회에 성공한다.")
    @Test
    void getAdminReservationPage() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("관리자 시간 관리 페이지 조회에 성공한다.")
    @Test
    void getAdminReservationTimePage() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("관리자 테마 페이지 조회에 성공한다.")
    @Test
    void getAdminThemePage() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("관리자 예약 대기 페이지 조회에 성공한다.")
    @Test
    void getAdminWaitingPage() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/waiting")
                .then().log().all()
                .statusCode(200);
    }
}
