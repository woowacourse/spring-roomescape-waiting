package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClientPageTest {
    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("client reservaton 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetClientReservationPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("index 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetIndexPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("로그인 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetLoginPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("회원가입 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetSignUpPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .when().get("/signup")
                .then().log().all()
                .statusCode(200);
    }
}


