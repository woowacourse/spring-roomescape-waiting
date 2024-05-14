package roomescape.controller.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.util.TokenGenerator;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberApiControllerTest {

    @Test
    @DisplayName("유저 목록 조회 요청이 정상적으로 수행된다.")
    void selectMembers_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
