package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberControllerTest extends IntegrationTest {
    @Nested
    @DisplayName("사용자 목록 조회 API")
    class FindAllMember {
        @Test
        void 사용자_목록을_조회할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }
}
