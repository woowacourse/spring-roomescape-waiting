package roomescape.view.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;
import roomescape.fixture.LoginMemberFixture;
import roomescape.member.domain.Member;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UserPageControllerTest {

    @DisplayName("유저용 reservation 페이지를 반환한다")
    @Test
    void reservationPageTest() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("login 페이지를 반환한다")
    @Test
    void loginPageTest() {
        RestAssured.given().log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("signup 페이지를 반환한다")
    @Test
    void signupPageTest() {
        RestAssured.given().log().all()
                .when().get("/signup")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("reservation-mine 페이지를 반환한다")
    @Test
    @Sql("/test-data.sql")
    void reservationMinePageTest() {
        Member user = LoginMemberFixture.getUser();
        String userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(user.getPassword(), user.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        RestAssured.given().log().all()
                .header("Cookie", userCookie)
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(200);
    }
}
