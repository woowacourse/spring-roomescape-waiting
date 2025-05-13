package roomescape.auth.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoginRestControllerTest {

    @Test
    void 로그인에_성공하면_쿠키와_토큰을_반환한다() {
        // given
        Map<String, String> loginRequest = Map.of(
                "email", "wooga@gmail.com",
                "password", "1234"
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .cookie("token")
                .body("accessToken", notNullValue());
    }

    @Test
    void 로그인하지_않고_로그인_상태_체크시_UNAUTHORIZED_반환() {
        RestAssured.given().log().all()
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 로그인한_후_로그인_상태_체크시_성공한다() {
        // given
        Map<String, String> loginRequest = Map.of(
                "email", "wooga@gmail.com",
                "password", "1234"
        );

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        // when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("WooGa"));
    }
}
