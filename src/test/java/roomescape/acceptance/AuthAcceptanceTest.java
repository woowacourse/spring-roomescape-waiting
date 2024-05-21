package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.*;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.dto.MemberResponse;

public class AuthAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("로그인을 하여 얻은 accessToken으로 사용자의 정보를 받아온다.")
    void tokenLoginAndFindMemberInfo() {
        final String accessToken = getAccessToken(MEMBER_MIA_EMAIL);

        final MemberResponse response = RestAssured
                .given().log().all()
                .cookie("token", accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .extract().as(MemberResponse.class);

        assertThat(response.name()).isEqualTo(MEMBER_MIA_NAME);
    }

    @Test
    @DisplayName("로그아웃에 성공하면 200을 응답한다.")
    void respondOkWhenLogout() {
        final String accessToken = getAccessToken(MEMBER_MIA_EMAIL);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200);
    }
}
