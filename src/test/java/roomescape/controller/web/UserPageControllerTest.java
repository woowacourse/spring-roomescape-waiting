package roomescape.controller.web;

import static roomescape.TokenTestFixture.USER_TOKEN;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UserPageControllerTest {

    @DisplayName("메인 페이지 응답 -> 200")
    @Test
    void getMainPage() {
        RestAssured.given().log().all()
            .when().get("/")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("/reservation 페이지 응답 -> 200")
    @Test
    void getReservationPage() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .when().get("/reservation")
            .then().log().all()
            .statusCode(200);
    }
}
