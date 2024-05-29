package roomescape.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import roomescape.controller.BaseControllerTest;
import roomescape.controller.dto.request.LoginRequest;

class AuthControllerTest extends BaseControllerTest {

    private String token;

    @TestFactory
    @DisplayName("로그인, 로그인 상태 확인, 로그아웃을 한다.")
    Stream<DynamicTest> authControllerTests() {
        return Stream.of(
                DynamicTest.dynamicTest("로그인한다.", this::login),
                DynamicTest.dynamicTest("로그인 상태를 확인한다.", this::checkLogin),
                DynamicTest.dynamicTest("로그아웃한다.", this::logout)
        );
    }

    @Test
    @DisplayName("로그인하지 않으면 로그인 상태를 확인할 수 없다.")
    void checkLoginFailWhenNotLoggedIn() {
        String token = "invalid token";

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    void login() {
        LoginRequest request = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);

        token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .cookie("token");

        assertThat(token).isNotNull();
    }

    void checkLogin() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    void logout() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().post("/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
