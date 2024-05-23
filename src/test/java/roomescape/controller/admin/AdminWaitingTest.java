package roomescape.controller.admin;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.TestDataInitExtension;
import roomescape.auth.AuthorizationExtractor;
import roomescape.controller.TestAccessToken;

/*
 * 예약 대기 초기 데이터
 * {ID=1, DATE='2024-04-30', TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=2, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=2, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=3, STATUS=WAITING}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestDataInitExtension.class)
class AdminWaitingTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestAccessToken testAccessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 대기 목록을 불러오는데 성공하면 200을 응답한다.")
    void given_when_getWaiting_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin/waitings")
                .then().log().all()
                .body("size()", is(2))
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 대기 삭제 성공 시 204를 응답한다.")
    void given_when_deleteSuccessful_then_statusCodeIsNoContents() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().delete("/admin/waitings/1")
                .then().log().all()
                .statusCode(204);
    }
}
