package roomescape.controller;

import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.MEMBER1_LOGIN_REQUEST;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.TestFixture;
import roomescape.domain.Role;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.AuthInfo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("로그인에 성공한다.")
    @Test
    void login() {
        // given
        memberRepository.save(MEMBER1);

        // when & then
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(MEMBER1_LOGIN_REQUEST)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("로그인 정보가 존재하지 않으면 로그인 페이지로 리다이렉트된다.")
    @Test
    void checkLogin() {
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new AuthInfo(1L, "test", Role.MEMBER))
                .when().get("/login/check")
                .then().log().all()
                .body(Matchers.containsString("<title>Login</title>"));
    }

    @DisplayName("로그아웃에 성공한다.")
    @Test
    void logout() {
        // given
        memberRepository.save(MEMBER1);
        String accessToken = TestFixture.getTokenAfterLogin(MEMBER1_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().post("/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .cookie("token", "");
    }
}
