package roomescape.integration.api.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.common.RestAssuredTestBase;

class MemberPageTest extends RestAssuredTestBase {

    @Test
    void 유저_예약하기_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", generateLoginMember().sessionId())
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 기본페이지인_인기_테마_페이지_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", generateLoginMember().sessionId())
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }
}
