package roomescape.domain.login.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ControllerTest;

class LoginControllerTest extends ControllerTest {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "insert into member (name, email, password, role) values ('어드민', 'admin@gmail.com', '123456', 'ADMIN')");
    }

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
