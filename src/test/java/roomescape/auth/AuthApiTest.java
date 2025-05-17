package roomescape.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;
import roomescape.global.auth.JwtTokenProvider;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-member-data.sql")
public class AuthApiTest {

    public static final String TOKEN_COOKIE_NAME = "token";

    @DisplayName("로그인 테스트")
    @Nested
    class LoginTest {

        @DisplayName("올바른 이메일과 비밀번호를 입력하면 200을 반환한다")
        @Test
        void testLogin() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("aaa@gmail.com", "1234"))
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(200)
                    .header("Set-Cookie", Matchers.notNullValue());
        }

        @DisplayName("이메일 또는 비밀번호 정보가 올바르지 않으면 401을 반환한다.")
        @Test
        void testInvalidEmailOrPassword() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("ddd@gmail.com", "1234"))
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(401);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("aaa@gmail.com", "1111"))
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다.")
        @Test
        void testInvalidEmailFormat() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("ddd", "1234"))
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @DisplayName("인증 정보 조회 API 테스트")
    @Nested
    class LoginCheckTest {

        @DisplayName("인증 정보 조회를 성공할 경우 200을 반환한다.")
        @Test
        void testLoginCheck() {
            // given
            String token = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("aaa@gmail.com", "1234"))
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(200)
                    .extract().cookie(TOKEN_COOKIE_NAME);
            // when
            // then
            RestAssured.given().log().all()
                    .cookie(TOKEN_COOKIE_NAME, token)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(200)
                    .body("name", Matchers.equalTo("사용자1"));
        }

        @DisplayName("쿠키를 찾을 수 없는 경우 401을 반환한다.")
        @Test
        void testCookieAbsence() {
            RestAssured.given().log().all()
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(401);
            RestAssured.given().log().all()
                    .cookie("invalidName", TOKEN_COOKIE_NAME)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("쿠키의 정보가 올바르지 않을 경우 401을 반환한다.")
        @Test
        void testInvalidCookie() {
            RestAssured.given().log().all()
                    .cookie(TOKEN_COOKIE_NAME, "3L")
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("사용자를 찾을 수 없는 경우 401을 반환한다.")
        @Test
        void testNotFoundMember() {
            // given
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
            String token = jwtTokenProvider.createToken("4");
            // when
            // then
            RestAssured.given().log().all()
                    .cookie(TOKEN_COOKIE_NAME, token)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(401);
        }
    }

    @DisplayName("로그아웃 테스트")
    @Nested
    class LogoutTest {

        @DisplayName("로그아웃에 성공할 경우 204를 반환한다.")
        @Test
        void testLogout() {
            // given
            String token = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("aaa@gmail.com", "1234"))
                    .when().post("/login")
                    .then().log().all()
                    .extract().cookie(TOKEN_COOKIE_NAME);
            // when
            // then
            RestAssured.given().log().all()
                    .cookie(TOKEN_COOKIE_NAME, token)
                    .when().post("/logout")
                    .then().log().all()
                    .statusCode(204)
                    .cookie(TOKEN_COOKIE_NAME, "");
        }

        @DisplayName("토큰 정보가 올바르지 않을 경우 401을 반환한다.")
        @Test
        void testInvalidToken() {
            RestAssured.given().log().all()
                    .when().post("/logout")
                    .then().log().all()
                    .statusCode(401);
        }
    }
}
