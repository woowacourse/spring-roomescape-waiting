package roomescape.domain.login.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.ControllerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

class LoginControllerTest extends ControllerTest {

    @DisplayName("로그인 요청을 하면 쿠키와 함께 응답한다.(200 OK)")
    @Test
    void should_response_with_cookie_when_request_login() {
        String cookie = getAdminCookie();

        assertThat(cookie).isNotNull();
    }

    @DisplayName("Cookie와 함께 사용자 정보 조회 시, name과 함께 응답받는다.")
    @Test
    void should_response_with_name_when_get_check_member_with_cookie() {
        String cookie = getAdminCookie();
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", is("어드민"));
    }
}
