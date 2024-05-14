package roomescape.core.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class MemberControllerTest {
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "password";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 페이지로 이동한다.")
    void moveToReservationPage() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("로그인 페이지로 이동한다.")
    void moveToLoginPage() {
        RestAssured.given().log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("로그인을 수행한다.")
    void login() {
        TokenRequest request = new TokenRequest("test@email.com", "password");

        RestAssured.given().log().all()
                .contentType("application/json")
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("인증 정보를 확인한다.")
    void checkLogin() {
        String accessToken = RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");

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
        RestAssured.given().log().all()
                .when().post("/logout")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("회원 가입 페이지로 이동한다.")
    void moveToSignupPage() {
        RestAssured.given().log().all()
                .when().get("/signup")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("회원 가입을 수행한다.")
    void signup() {
        final MemberRequest request = new MemberRequest("hello@email.com", "password", "test");

        RestAssured.given().log().all()
                .contentType("application/json")
                .body(request)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 가입되어 있는 이메일로 가입하면 예외가 발생한다.")
    void signupWithDuplicatedEmail() {
        final MemberRequest request = new MemberRequest("test@email.com", "password", "test");

        RestAssured.given().log().all()
                .contentType("application/json")
                .body(request)
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("모든 회원 정보를 조회한다.")
    void findMembers() {
        RestAssured.given().log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200);
    }
}
