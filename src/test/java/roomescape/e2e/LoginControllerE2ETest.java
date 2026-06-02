package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class LoginControllerE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        seedMember("유저", "user@test.com", "USER");
    }

    @Nested
    class Login {

        @Test
        @DisplayName("올바른 자격증명으로 로그인하면 200을 반환하고 세션을 발급한다")
        void loginSuccess() {
            String sessionId = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "user@test.com", "password", PASSWORD_PLAIN))
                    .when().post("/sessions")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().sessionId();

            org.assertj.core.api.Assertions.assertThat(sessionId).isNotBlank();
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인하면 400을 반환한다")
        void loginWrongPassword() {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "user@test.com", "password", "wrong"))
                    .when().post("/sessions")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인하면 400을 반환한다")
        void loginUnknownEmail() {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "ghost@test.com", "password", PASSWORD_PLAIN))
                    .when().post("/sessions")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class Signup {

        @Test
        @DisplayName("새 멤버를 가입하면 201을 반환한다")
        void signupSuccess() {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "신규", "email", "new@test.com", "password", "secret"))
                    .when().post("/members")
                    .then().statusCode(HttpStatus.CREATED.value());
        }

        @Test
        @DisplayName("중복된 이메일로 가입하면 409를 반환한다")
        void signupDuplicate() {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "중복", "email", "user@test.com", "password", "secret"))
                    .when().post("/members")
                    .then().statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("이메일 형식이 아니면 400을 반환한다")
        void signupInvalidEmail() {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "잘못", "email", "not-email", "password", "secret"))
                    .when().post("/members")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class Logout {

        @Test
        @DisplayName("로그아웃하면 200을 반환한다")
        void logoutSuccess() {
            String sessionId = loginAs("user@test.com");

            RestAssured.given()
                    .sessionId(sessionId)
                    .when().delete("/sessions")
                    .then().statusCode(HttpStatus.OK.value());
        }
    }

    @Nested
    class Me {

        @Test
        @DisplayName("로그인 후 /members/me를 조회하면 본인 정보를 반환한다")
        void returnsCurrentMember() {
            String sessionId = loginAs("user@test.com");

            RestAssured.given()
                    .sessionId(sessionId)
                    .when().get("/members/me")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("name", org.hamcrest.Matchers.equalTo("유저"))
                    .body("email", org.hamcrest.Matchers.equalTo("user@test.com"))
                    .body("role", org.hamcrest.Matchers.equalTo("USER"));
        }

        @Test
        @DisplayName("세션 없이 /members/me를 조회하면 401을 반환한다")
        void unauthenticated() {
            RestAssured.given()
                    .when().get("/members/me")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
