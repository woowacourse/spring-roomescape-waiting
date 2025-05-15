package roomescape.controller.page;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Test
    @DisplayName("/reservation 요청시 사용자 예약 페이지 응답")
    void reservationPage() {
        RestAssured.given().log().all()
            .when().get("/reservation")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("/signup 요청시 사용자 회원가입 페이지 응답")
    void signupPage() {
        RestAssured.given().log().all()
            .when().get("/signup")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("/login 요청시 사용자 로그인 페이지 응답")
    void loginPage() {
        RestAssured.given().log().all()
            .when().get("/login")
            .then().log().all()
            .statusCode(200);
    }
}
