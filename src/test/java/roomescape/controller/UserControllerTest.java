package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.request.UserLoginRequest;
import roomescape.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UserControllerTest {

    @DisplayName("로그인 요청시 쿠키를 응답한다.")
    @Test
    void should_response_cookie_when_login() {
        UserLoginRequest request = new UserLoginRequest("1234", "sun@email.com");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");

        assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(cookie).isNotBlank();
            assertSoftly.assertThat(cookie).containsPattern("^token=");
        });
    }

    @DisplayName("요청시 쿠키를 제공하면 이름을 반환한다.")
    @Test
    void should_response_user_name_when_given_cookie() {
        UserLoginRequest request = new UserLoginRequest("1234", "sun@email.com");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");

        String name = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/login/check")
                .then().statusCode(200)
                .extract().jsonPath().get("name");

        assertThat(name).isEqualTo("썬");
    }

    @DisplayName("모든 사용자들을 반환한다.")
    @Test
    void should_response_all_users() {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().statusCode(200)
                .extract().jsonPath().getList(".", User.class);
    }
}
