package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminPageTest {
    private static final String EMAIL = "testDB@email.com";
    private static final String PASSWORD = "1234";
    @LocalServerPort
    int port;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        accessToken = RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @DisplayName("admin 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetAdminPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("reservation 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetReservationPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("time 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetTimePage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("theme 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetThemePage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }
}
