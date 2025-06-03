package roomescape.integration.api.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.common.RestAssuredTestBase;

public class MyPageTest extends RestAssuredTestBase {

    @Test
    void 자신의_예약정보_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", generateLoginMember().sessionId())
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(200);
    }
}
