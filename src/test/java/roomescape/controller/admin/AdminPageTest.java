package roomescape.controller.admin;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.AuthorizationExtractor;
import roomescape.controller.TestAccessToken;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminPageTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestAccessToken testAccessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("admin 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetAdminPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("reservation 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetReservationPage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("time 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetTimePage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("theme 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetThemePage_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("일반 계정이 admin/** 페이지 URL 요청시 연결되지 않는다.")
    @Test
    void given_userCookie_when_GetThemePage_then_statusCodeIsUnauthorized() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("쿠키가 없으면 admin/** 페이지 URL 요청시 연결되지 않는다.")
    @Test
    void given_emptyCookie_when_GetThemePage_then_statusCodeIsUnauthorized() {
        RestAssured.given().log().all()
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("waiting 페이지 URL 요청이 올바르게 연결된다.")
    @Test
    void given_adminToken_when_GetWaitingPage_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin/waiting")
                .then().log().all()
                .statusCode(200);
    }
}
