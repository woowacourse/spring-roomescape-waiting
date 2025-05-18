package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/data.sql")
class AuthTest {

    @Test
    void 로그인_요청시_set_cookie로_토큰을_받을_수_있다() {
        Map<String, String> loginParams = Map.of("email", "admin@naver.com", "password", "1234");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when()
                .post("/login")
                .then().log().all()
                .statusCode(200)
                .extract();

        assertThat(response.cookie("token")).isNotEmpty();
    }

    @Test
    void 존재하지_않는_회원이_로그인을_시도하면_예외_발생() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "InvalidMember@naver.com", "password", "1234"))
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 어드민_회원이_어드민_페이지에_접근_시_통과() {
        // given
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 일반_회원이_어드민_페이지에_접근_시_예외_발생() {
        // given
        Map<String, String> normalUser = Map.of("email", "member@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(normalUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 정상적으로_로그아웃() {
        // given
        Map<String, String> normalUser = Map.of("email", "member@naver.com", "password", "1234");
        final String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(normalUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().post("/logout")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 쿠키가_존재_한다면_통과() {
        // given
        Map<String, String> normalUser = Map.of("email", "member@naver.com", "password", "1234");
        final String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(normalUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 쿠키가_존재하지_않는다면_로그인_체크할_때_예외_발생() {
        RestAssured.given().log().all()
                .cookie("token", "")
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }
}
