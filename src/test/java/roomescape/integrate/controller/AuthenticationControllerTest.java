package roomescape.integrate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.dto.member.MemberResponseDto;
import roomescape.integrate.IntegrationTest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest extends IntegrationTest {

    @Test
    void 회원가입_테스트() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "signup@email.com", "password", "password");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then()
                .statusCode(201);
    }

    @Test
    void 유저_정보_확인_테스트() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login2@email.com", "password",
                "password");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then()
                .statusCode(201);

        Map<String, String> loginParam = Map.of("email", "token-login2@email.com", "password", "password");
        String header = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParam)
                .when().post("/auth/login")
                .then().log().all()
                .extract().header("Set-Cookie");

        String token = header.split("token=")[1];

        MemberResponseDto memberResponseDto = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/auth/login/check")
                .then().log().all()
                .extract().body().as(MemberResponseDto.class);

        assertThat(memberResponseDto.name()).isEqualTo("투다");
    }

    @Test
    void jwt_토큰_로그인_테스트() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login3@email.com", "password",
                "password");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then()
                .statusCode(201);

        Map<String, String> loginParam = Map.of("email", "token-login3@email.com", "password", "password");
        String header = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParam)
                .when().post("/auth/login")
                .then().log().all()
                .extract().header("Set-Cookie");

        String token = header.split("token=")[1];

        assertAll(
                () -> assertThat(header).isNotNull(),
                () -> assertThat(token).isNotEmpty()
        );
    }

    @Test
    void 회원가입_이메일_형식이_아니면_예외발생() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login3", "password",
                "password");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 회원가입_패스워드_크기가_유효하지_않으면_예외_발생() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login@naver.com", "password",
                "as");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 같은_이메일을_가질_수_없음() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login@naver.com", "password",
                "password");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then().log().all()
                .statusCode(201);

        Map<String, String> duplicateSignupParam = Map.of("name", "투다2", "email", "token-login@naver.com", "password2",
                "password");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(duplicateSignupParam)
                .when().post("/auth/signup")
                .then().log().all()
                .statusCode(400);
    }
}

