package roomescape.view.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import roomescape.auth.dto.LoginRequest;

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
    void reservationMinePageTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("email", "brown@gmail.com");
        params.put("password", "wooteco7");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().log().all()
                .statusCode(200);

        String userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest("wooteco7", "brown@gmail.com"))
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
