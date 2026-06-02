package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.TokenResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/cleanup.sql")
@SqlMergeMode(MergeMode.MERGE)
public class AuthControllerTest {

    private static final String INSERT_SINGLE_MEMBER_SQL = """
            INSERT INTO member (id, email, password, name)
            VALUES (1, 'brown@email.com', 'password', '브라운');
            """;

    private static final String USERNAME_FIELD = "email";
    private static final String PASSWORD_FIELD = "password";
    private static final String EMAIL = "brown@email.com";
    private static final String PASSWORD = "password";
    private static final String NAME = "브라운";

    @Nested
    class 쿠키_로그인 {

        @Test
        @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
        void 성공하면_access_token_쿠키가_HttpOnly로_발급된다() {
            String setCookie = RestAssured
                    .given().log().all()
                    .param(USERNAME_FIELD, EMAIL)
                    .param(PASSWORD_FIELD, PASSWORD)
                    .when().post("/api/v1/auth/login")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().header("Set-Cookie");

            assertThat(setCookie).isNotBlank();
            assertThat(setCookie).contains("access_token=");
            assertThat(setCookie).contains("HttpOnly");
        }

        @Test
        @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
        void 비밀번호가_틀리면_401을_반환한다() {
            RestAssured
                    .given().log().all()
                    .param(USERNAME_FIELD, EMAIL)
                    .param(PASSWORD_FIELD, "wrong-password")
                    .when().post("/api/v1/auth/login")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("errorCode", is("AUTH401_001"));
        }

        @Test
        void 존재하지_않는_이메일로_로그인하면_401을_반환한다() {
            RestAssured
                    .given().log().all()
                    .param(USERNAME_FIELD, "nobody@email.com")
                    .param(PASSWORD_FIELD, PASSWORD)
                    .when().post("/api/v1/auth/login")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("errorCode", is("AUTH401_001"));
        }
    }

    @Nested
    class 토큰_로그인 {

        @Test
        @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
        void 성공하면_JWT가_JSON_응답으로_발급된다() {
            TokenResponse response = RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest(EMAIL, PASSWORD))
                    .when().post("/api/v1/auth/login/token")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(TokenResponse.class);

            assertThat(response.token()).isNotBlank();
        }

        @Test
        @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
        void 비밀번호가_틀리면_401을_반환한다() {
            RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest(EMAIL, "wrong-password"))
                    .when().post("/api/v1/auth/login/token")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("errorCode", is("AUTH401_001"));
        }
    }

    @Nested
    class 현재_사용자_조회 {

        @Test
        @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
        void 쿠키로_로그인된_사용자_정보를_조회할_수_있다() {
            String cookie = RestAssured
                    .given().log().all()
                    .param(USERNAME_FIELD, EMAIL)
                    .param(PASSWORD_FIELD, PASSWORD)
                    .when().post("/api/v1/auth/login")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().header("Set-Cookie").split(";")[0];

            RestAssured
                    .given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/auth/me")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", is(1))
                    .body("email", is(EMAIL))
                    .body("name", is(NAME));
        }

        @Test
        @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
        void Authorization_헤더로_로그인된_사용자_정보를_조회할_수_있다() {
            String accessToken = RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest(EMAIL, PASSWORD))
                    .when().post("/api/v1/auth/login/token")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(TokenResponse.class)
                    .token();

            MemberResponse member = RestAssured
                    .given().log().all()
                    .auth().oauth2(accessToken)
                    .when().get("/api/v1/auth/me")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(MemberResponse.class);

            assertThat(member.id()).isEqualTo(1L);
            assertThat(member.email()).isEqualTo(EMAIL);
            assertThat(member.name()).isEqualTo(NAME);
        }

        @Test
        void 비로그인_상태로_조회하면_401을_반환한다() {
            RestAssured
                    .given().log().all()
                    .when().get("/api/v1/auth/me")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("errorCode", is("AUTH401_002"));
        }

        @Test
        void 잘못된_토큰으로_조회하면_401을_반환한다() {
            RestAssured
                    .given().log().all()
                    .auth().oauth2("invalid.jwt.token")
                    .when().get("/api/v1/auth/me")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("errorCode", is("AUTH401_003"));
        }
    }

    @Nested
    class 로그아웃 {

        @Test
        @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
        void 로그아웃하면_access_token_쿠키가_폐기된다() {
            String cookie = RestAssured
                    .given().log().all()
                    .param(USERNAME_FIELD, EMAIL)
                    .param(PASSWORD_FIELD, PASSWORD)
                    .when().post("/api/v1/auth/login")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().header("Set-Cookie").split(";")[0];

            String clearedCookie = RestAssured
                    .given().log().all()
                    .header("Cookie", cookie)
                    .when().post("/api/v1/auth/logout")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .extract().header("Set-Cookie");

            assertThat(clearedCookie).contains("access_token=");
            assertThat(clearedCookie).contains("Max-Age=0");
        }
    }
}
