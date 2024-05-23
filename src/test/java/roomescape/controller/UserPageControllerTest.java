package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.IntegrationTestSupport;
import roomescape.controller.member.dto.CookieMemberResponse;
import roomescape.controller.member.dto.MemberLoginRequest;

import static org.assertj.core.api.Assertions.assertThat;

class UserPageControllerTest extends IntegrationTestSupport {

    @Test
    @DisplayName("사용자 로그인")
    void showUserPage() {
        RestAssured.given().log().all()
                .contentType("application/json")
                .body(new MemberLoginRequest("jinwuo0925@gmail.com", "1111"))
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("어드민 정보 확인")
    void checkAdmin() {
        final CookieMemberResponse memberResponse = RestAssured.given().log().all()
                .cookie("token", USER_TOKEN)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200).extract().as(CookieMemberResponse.class);

        assertThat(memberResponse.name()).isEqualTo(USER_NAME);
    }

    @Test
    @DisplayName("로그인 후 로그아웃")
    void loginAndlogout() {
        RestAssured.given().log().all()
                .cookie("token", USER_TOKEN)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("회원 가입 화면 조회")
    void showSignup() {
        RestAssured.given().log().all()
                .contentType("application/json")
                .when().get("/signup")
                .then().log().all()
                .statusCode(200);
    }
}
