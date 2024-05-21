package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.IntegrationTestSupport;

class AdminPageControllerTest extends IntegrationTestSupport {

    @DisplayName("어드민 권한이 있으면 어드민 페이지에 접근할 수 있다.")
    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/time", "/admin/theme"})
    void admin(String url) {
        RestAssured.given().log().all()
                .cookie("token", ADMIN_TOKEN)
                .when().get(url)
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("유저는 어드민 페이지에 접근할 수 없다.")
    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/time", "/admin/theme"})
    void user(String url) {
        RestAssured.given().log().all()
                .cookie("token", USER_TOKEN)
                .when().get(url)
                .then().log().all()
                .statusCode(404);
    }
}
