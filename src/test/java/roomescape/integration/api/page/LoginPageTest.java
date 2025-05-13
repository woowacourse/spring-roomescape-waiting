package roomescape.integration.api.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.common.RestAssuredTestBase;

class LoginPageTest extends RestAssuredTestBase {

    @Test
    void 로그인_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }
}
