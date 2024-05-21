package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.service.dto.LoginCheckResponse;
import roomescape.service.dto.LoginRequest;

class LoginIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("로그인 API")
    class Login {
        @Test
        void 이메일과_비밀번호로_로그인할_수_있다() {
            RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(new LoginRequest("admin@email.com", "password"))
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(200);
        }

        @Test
        void 이메일이나_비밀번호가_틀리면_로그인할_수_없다() {
            RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(new LoginRequest("admin@email.com", "wrongpassword"))
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("인증 정보 조회 API")
    class LoginCheck {
        @Test
        void 토큰으로_로그인한_사용자_정보를_조회할_수_있다() {
            LoginCheckResponse response = RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value()).extract().as(LoginCheckResponse.class);

            assertThat(response.getName()).isEqualTo("어드민");
        }

        @Test
        void 토큰이_존재하지_않으면_예외가_발생한다() {
            RestAssured.given().log().all()
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 토큰이_유효하지_않으면_예외가_발생한다() {
            RestAssured.given().log().all()
                    .header("Cookie", "token=asdfadsfcx.safsdf.scdsafd")
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class Logout {
        @Test
        void 토큰으로_로그아웃_할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().post("/logout")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());
        }
    }

    @Nested
    @DisplayName("회원가입 API")
    class Signup {
        @Test
        void 일반유저_권한으로_회원가입을_할_수_있다() {
            Map<String, String> params = new HashMap<>();
            params.put("email", "user@email.com");
            params.put("password", "password");
            params.put("name", "사용자");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/signup")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/members/2")
                    .body("id", is(2))
                    .body("role", is("USER"));
        }
    }
}
