package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.dto.TokenCookieDto;

import static org.hamcrest.Matchers.is;

class MemberControllerTest extends IntegrationTest {

    @Test
    @DisplayName("/members 으로 GET 요청을 보내면 회원 정보와 200 OK 를 받는다.")
    void getAdminPage() {
        // given
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("admin@admin.com", "12341234", port);
        memberFixture.createMember();
        memberFixture.createMember();

        // when & then
        RestAssured.given().log().all()
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(200)
                .body("data.members.size()", is(3));
    }
}
