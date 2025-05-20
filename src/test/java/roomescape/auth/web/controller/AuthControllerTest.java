package roomescape.auth.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.auth.web.constant.AuthConstant.AUTH_COOKIE_KEY;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.ReservationTestFixture;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.web.controller.dto.LoginRequest;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.support.IntegrationTestSupport;

class AuthControllerTest extends IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;

    String name = "웨이드";
    String email = "wade@naver.com";
    String password = "1234";

    @BeforeEach
    void setUp() {
        Member member = ReservationTestFixture.createUser(name, email, password);
        memberRepository.save(member);
    }

    @DisplayName("로그인 성공시 쿠키를 반환한다")
    @Test
    void login() {
        //given
        LoginRequest request = new LoginRequest(email, password);

        //when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response();
        //then
        String authCookie = response.getCookie(AUTH_COOKIE_KEY);
        assertThat(authCookie).isNotBlank();
    }

    @DisplayName("로그인 이후 토큰으로 로그인 체크를 할 수 있다")
    @Test
    void loginCheck() {
        //given
        LoginRequest request = new LoginRequest(email, password);

        String authCookie = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .cookie(AUTH_COOKIE_KEY);

        //when
        AuthenticatedMember result = RestAssured.given()
                .cookie(AUTH_COOKIE_KEY, authCookie)
                .get("/login/check")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(AuthenticatedMember.class);

        //then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(email);
    }

    @DisplayName("로그아웃 시 쿠키가 만료된다")
    @Test
    void logout() {
        //given
        LoginRequest request = new LoginRequest(email, password);

        String authCookie = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .cookie(AUTH_COOKIE_KEY);

        //when
        Cookie cookie = RestAssured.given()
                .cookie(AUTH_COOKIE_KEY, authCookie)
                .post("/logout")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response().detailedCookie(AUTH_COOKIE_KEY);

        //then
        assertThat(cookie.getMaxAge()).isEqualTo(0);
    }
}
