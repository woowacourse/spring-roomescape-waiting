package roomescape.integration.view;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import roomescape.integration.IntegrationTest;

public class AdminPageIntegrationTest extends IntegrationTest {
    @Test
    void 어드민_메인_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookies(cookieProvider.createCookies())
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 예약_관리_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookies(cookieProvider.createCookies())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 시간_관리_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookies(cookieProvider.createCookies())
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }
}
