package roomescape.api;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Cookie.Builder;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthApiTest {

    private static final Map<String, Object> MEMBER_BODY = new HashMap<>();
    private static final Map<String, Object> AUTH_BODY = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public AuthApiTest(
            @LocalServerPort final int port,
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeAll
    static void beforeAll() {
        AUTH_BODY.put("email", "admin@email.com");
        AUTH_BODY.put("password", "password");

        MEMBER_BODY.put("email", "admin@email.com");
        MEMBER_BODY.put("password", "password");
        MEMBER_BODY.put("name", "어드민");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
    }

    @Nested
    @DisplayName("로그인")
    class Login {
        @DisplayName("로그인에 성공하면 200을 응답하고, Set-Cookie 헤더에 토큰이 담긴다.")
        @Test
        void post() {
            // given
            givenCreateMember(AUTH_BODY.get("password").toString());

            // when & then
            RestAssured.given().port(port).log().all()
                    .contentType(ContentType.JSON)
                    .body(AUTH_BODY)
                    .when().post("/login")
                    .then().log().all().statusCode(200)
                    .header(HttpHeaders.SET_COOKIE.toString(), notNullValue());
        }

        @DisplayName("존재하지 않는 이메일이라면, 400을 응답한다.")
        @Test
        void post1() {
            // given & when & then
            RestAssured.given().port(port).log().all()
                    .contentType(ContentType.JSON)
                    .body(AUTH_BODY)
                    .when().post("/login")
                    .then().log().all().statusCode(400)
                    .header(HttpHeaders.SET_COOKIE.toString(), nullValue());
        }

        @DisplayName("비밀번호가 일치하지 않는다면, 400을 응답한다.")
        @Test
        void post2() {
            givenCreateMember("another password");

            // given & when & then
            RestAssured.given().port(port).log().all()
                    .contentType(ContentType.JSON)
                    .body(AUTH_BODY)
                    .when().post("/login")
                    .then().log().all().statusCode(400)
                    .header(HttpHeaders.SET_COOKIE.toString(), nullValue());
        }
    }

    @Nested
    @DisplayName("토큰기반 유저 정보 조회")
    class Check {
        @DisplayName("check에 성공하면, 200OK를 응답한다.")
        @Test
        void check() {
            // given
            givenCreateMember(AUTH_BODY.get("password").toString());
            final Cookie cookie = givenAuthCookie();

            // when & then
            RestAssured.given().log().all().port(port)
                    .cookie(cookie)
                    .when().get("/login/check")
                    .then().log().all().statusCode(200);
        }

        @DisplayName("유효하지 않은 토큰으로 요청하면, 401을 응답한다.")
        @Test
        void check1() {
            // given
            final Cookie token = new Builder("token",
                    "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJub3RGb3VuZEVtYWlsQGVtYWlsLmNvbSIsImlhdCI6MTc0NjQ0NDI4MywiZXhwIjoxNzQ2NDQ0MzQzfQ.yLtLrHkOiYsh0ZIG1g4RcQWoqe9STMvQxZtAsU9U4WU").build();

            // when & then
            RestAssured.given().log().all().port(port)
                    .cookie(token)
                    .when().get("/login/check")
                    .then().log().all().statusCode(401);
        }
    }

    @DisplayName("쿠키를 사용하는 api에 대해 쿠키가 존재하지 않는다면, 400을 응답한다.")
    @Test
    void interceptor1() {
        RestAssured.given().log().all().port(port)
                .when().get("/admin/reservation")
                .then().log().all().statusCode(400);
    }

    @DisplayName("쿠키를 사용하는 api에 대해 token 키를 가진 쿠키가 존재하지 않는다면, 400을 응답한다.")
    @Test
    void interceptor2() {
        // given
        final Cookie notToken = new Builder("not token", "this is not token").build();

        // when & then
        RestAssured.given().log().all().port(port)
                .cookie(notToken)
                .when().get("/admin/reservation")
                .then().log().all().statusCode(400);
    }


    private void givenCreateMember(final String password) {
        final Map<String, Object> body = new HashMap<>(MEMBER_BODY);
        body.put("password", password);

        RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/members")
                .then().statusCode(201);
    }

    private Cookie givenAuthCookie() {
        return RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(AUTH_BODY)
                .when().post("/login")
                .then()
                .extract().detailedCookie("token");
    }
}
