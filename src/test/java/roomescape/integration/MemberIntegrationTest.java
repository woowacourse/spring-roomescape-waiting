package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.request.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/init-data.sql", "/controller-test-data.sql"})
class MemberIntegrationTest {

    @DisplayName("로그인 요청시 쿠키를 응답한다.")
    @Test
    void should_response_cookie_when_login() {
        MemberLoginRequest request = new MemberLoginRequest("1234", "sun@email.com");

        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .header("Set-Cookie", matchesPattern("^token=.*"));
    }

    @DisplayName("요청시 쿠키를 제공하면 이름을 반환한다.")
    @Test
    void should_response_member_name_when_given_cookie() {
        MemberLoginRequest request = new MemberLoginRequest("1234", "sun@email.com");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");

        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/login/check")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .body("name", is("썬"));
    }

    @DisplayName("모든 사용자들을 반환한다.")
    @Test
    void should_response_all_members() {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .body("size()", equalTo(3));
    }
}
