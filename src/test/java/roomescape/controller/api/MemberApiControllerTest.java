package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.controller.BaseControllerTest;
import roomescape.util.TokenGenerator;

class MemberApiControllerTest extends BaseControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

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
