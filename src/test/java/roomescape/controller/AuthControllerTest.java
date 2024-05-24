package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.request.LoginRequest;
import roomescape.controller.response.LoginResponse;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.model.member.Role;
import roomescape.util.TokenManager;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AuthControllerTest {

    private static final String LOGIN_NAME = "에버";
    private static final String LOGIN_EMAIL = "treeboss@gmail.com";
    private static final String LOGIN_PASSWORD = "treeboss123!";
    private static final String LOGIN_TOKEN = TokenManager.create(
            new MemberWithoutPassword(1L, LOGIN_NAME, LOGIN_EMAIL, Role.USER));

    @DisplayName("로그인을 성공할 경우 사용자 정보를 바탕으로 토큰을 생성하여 쿠키에 담아 반환한다.")
    @Test
    void should_return_token_through_cookie_when_login_success() {
        String actual = RestAssured
                .given().log().all()
                .body(new LoginRequest(LOGIN_EMAIL, LOGIN_PASSWORD))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        assertThat(actual).isEqualTo(LOGIN_TOKEN);
    }

    @DisplayName("로그인 된 계정의 사용자 정보를 반환한다.")
    @Test
    void should_return_name_of_login_member() {
        LoginResponse loginMember = RestAssured
                .given().log().all()
                .cookie("token", LOGIN_TOKEN)
                .when().get("/login/check")
                .then().log().all()
                .extract().body().as(LoginResponse.class);

        assertThat(loginMember.getName()).isEqualTo(LOGIN_NAME);
    }

    @DisplayName("로그아웃을 성공할 경우 토큰 쿠키를 삭제한다.")
    @Test
    void should_logout() {
        String tokenAfterLogout = RestAssured
                .given().log().all()
                .cookie("token", LOGIN_TOKEN)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        assertThat(tokenAfterLogout).isEmpty();
    }
}
