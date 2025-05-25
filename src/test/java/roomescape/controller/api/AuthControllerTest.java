package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginRequestDto;

import java.util.Map;
import roomescape.util.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Sql(scripts = {"/test-data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class AuthControllerTest {

    @Nested
    class MemberLoginTest {

        @DisplayName("등록된 회원이라면 로그인을 할 수 있다")
        @Test
        void loginMemberTest() {
            LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("등록되지 않은 회원은 로그인할 수 없다")
        @Test
        void loginNotJoinedMemberTest() {
            LoginRequestDto loginRequestDto = new LoginRequestDto("hello1@woowa.com", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(404);
        }

        @DisplayName("로그인한 상태를 확인할 수 있다")
        @Test
        void loginCheckMemberTest() {
            LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");

            Map<String, String> cookies = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .getCookies();

            String token = cookies.get("token");

            RestAssured.given().cookie("token", token).log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("토큰이 올바르지 않으면 로그인 상태를 유지할 수 없다")
        @Test
        void loginCheckInvalidTokenMemberTest() {
            LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");

            Map<String, String> cookies = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .getCookies();

            String token = cookies.get("token") + 1;

            RestAssured.given().cookie("token", token).log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(401);
        }
    }

    @Nested
    class MemberLogoutTest {

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        String loginToken;

        @BeforeEach
        void setUpRegistration() {
            loginToken = jwtTokenProvider.createToken(
                    new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password"));
        }

        @DisplayName("로그인 상태라면 로그아웃을 할 수있다")
        @Test
        void logoutMemberTest() {
            RestAssured.given().log().all()
                    .cookie(loginToken)
                    .contentType(ContentType.JSON)
                    .when().post("/logout")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("로그인 상태라면 로그아웃을 할 수있다")
        @Test
        void logoutMemberTest1() {
            RestAssured.given().log().all()
                    .cookie(loginToken)
                    .contentType(ContentType.JSON)
                    .when().post("/logout")
                    .then().log().all()
                    .statusCode(200);
        }
    }
}
