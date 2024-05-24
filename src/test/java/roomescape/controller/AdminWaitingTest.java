package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.dto.request.TokenRequest;

import static org.hamcrest.Matchers.is;
import static roomescape.fixture.fixture.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location=classpath:/application.properties"})
public class AdminWaitingTest {

    private static final String EMAIL = "testDB@email.com";
    private static final String PASSWORD = "1234";

    @LocalServerPort
    private int port;
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
                .then().log().cookies().extract().cookie(TOKEN);
    }

    @DisplayName("waiting 페이지에서 예약 대기 정보를 조회할 수 있다.")
    @Test
    void given_when_browseWaitings_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookies(TOKEN, accessToken)
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(WAITING_COUNT));
    }

    @DisplayName("waiting 페이지에서 예약 대기 정보를 거절할 수 있다.")
    @Test
    void given_when_turnDownWaitings_then_statusCodeIsNoContents() {
        RestAssured.given().log().all()
                .cookies(TOKEN, accessToken)
                .when().delete("admin/waitings/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookies(TOKEN, accessToken)
                .when().get("admin/waitings")
                .then().log().all()
                .body("size()", is(WAITING_COUNT - 1));
    }
}
