package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("사용자 목록 조회 API")
    class FindAllMember {
        @Test
        void 사용자_목록을_조회할_수_있다() {
            memberFixture.createUserMember();
            memberFixture.createAdminMember();

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(200)
                    .body("members.size()", is(2));
        }
    }
}
