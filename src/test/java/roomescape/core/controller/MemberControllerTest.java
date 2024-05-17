package roomescape.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.core.utils.e2eTest.getAccessToken;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.core.dto.auth.TokenRequest;
import roomescape.core.dto.member.MemberRequest;
import roomescape.core.dto.member.MemberResponse;
import roomescape.core.utils.e2eTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class MemberControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 페이지로 이동한다.")
    void moveToReservationPage() {
        ValidatableResponse response = e2eTest.get("/reservation");
        response.statusCode(200);
    }

    @Test
    @DisplayName("로그인 페이지로 이동한다.")
    void moveToLoginPage() {
        ValidatableResponse response = e2eTest.get("/login");
        response.statusCode(200);
    }

    @Test
    @DisplayName("로그인을 수행한다.")
    void login() {
        TokenRequest request = new TokenRequest("test@email.com", "password");
        ValidatableResponse response = e2eTest.post(request, "/login");
        response.statusCode(200);
    }

    @Test
    @DisplayName("인증 정보를 확인한다.")
    void checkLogin() {
        String accessToken = getAccessToken();

        MemberResponse user = RestAssured
                .given().log().all()
                .cookies("token", accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200).extract().as(MemberResponse.class);

        assertThat(user.getName()).isEqualTo("어드민");
    }

    @Test
    @DisplayName("로그아웃을 수행한다.")
    void logout() {
        ValidatableResponse response = e2eTest.post("/logout");
        response.statusCode(200);
    }

    @Test
    @DisplayName("회원 가입 페이지로 이동한다.")
    void moveToSignupPage() {
        ValidatableResponse response = e2eTest.get("/signup");
        response.statusCode(200);
    }

    @Test
    @DisplayName("회원 가입을 수행한다.")
    void signup() {
        final MemberRequest request = new MemberRequest("hello@email.com", "password", "test");

        ValidatableResponse response = e2eTest.post(request, "/members");
        response.statusCode(201);
    }

    @Test
    @DisplayName("이미 가입되어 있는 이메일로 가입하면 예외가 발생한다.")
    void signupWithDuplicatedEmail() {
        final MemberRequest request = new MemberRequest("test@email.com", "password", "test");

        ValidatableResponse response = e2eTest.post(request, "/members");
        response.statusCode(400);
    }

    @Test
    @DisplayName("모든 회원 정보를 조회한다.")
    void findMembers() {
        ValidatableResponse response = e2eTest.get("/members");
        response.statusCode(200);
    }

    @Test
    @DisplayName("로그인된 회원의 예약 목록 조회 페이지로 이동한다.")
    void findMyReservation() {
        ValidatableResponse response = e2eTest.get("/reservation-mine");
        response.statusCode(200);
    }
}
