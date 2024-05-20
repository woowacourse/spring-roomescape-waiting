package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthAcceptanceTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART");
    }

    @Test
    @DisplayName("로그인 API")
    void signup_API() {
        // given
        Map<String, String> member = Map.of("name", "aa", "email", "aa@aa.aa", "password", "aa");

        RestAssured
                .given().contentType(ContentType.JSON).body(member)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED)
                .log().all();

        // when & then
        Map<String, String> signupBody = Map.of("email", "aa@aa.aa", "password", "aa");

        RestAssured.given().contentType(ContentType.JSON).body(signupBody)
                .when().post("/login")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("로그인 API")
    void signup_API_failure() {
        // given

        Map<String, String> member = Map.of("name", "aa", "email", "aa@aa.aa", "password", "aa");

        RestAssured
                .given().contentType(ContentType.JSON).body(member)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED)
                .log().all();

        // when & then
        Map<String, String> signupBody = Map.of("email", "aa@aa.aa", "password", "bb");

        RestAssured.given().contentType(ContentType.JSON).body(signupBody)
                .when().post("/login")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("로그아웃 API")
    void logout_API() {
        // given
        Map<String, String> member = Map.of("name", "aa", "email", "aa@aa.aa", "password", "aa");

        RestAssured
                .given().contentType(ContentType.JSON).body(member)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED)
                .log().all();

        Map<String, String> signupBody = Map.of("email", "aa@aa.aa", "password", "aa");

        String token = RestAssured.given().contentType(ContentType.JSON).body(signupBody)
                .when().post("/login")
                .thenReturn().cookie("token");

        // when & then
        RestAssured.given().contentType(ContentType.JSON).cookie("token", token)
                .when().post("/logout")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("회원 인증 API")
    void member_authenticate_API() {
        // given
        Map<String, String> member = Map.of("name", "aa", "email", "aa@aa.aa", "password", "aa");

        RestAssured
                .given().contentType(ContentType.JSON).body(member)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED)
                .log().all();

        Map<String, String> signupBody = Map.of("email", "aa@aa.aa", "password", "aa");

        String token = RestAssured.given().contentType(ContentType.JSON).body(signupBody)
                .when().post("/login")
                .thenReturn().cookie("token");

        // when & then
        RestAssured.given().contentType(ContentType.JSON).cookie("token", token)
                .when().get("/login/check")
                .then().statusCode(HttpStatus.SC_OK)
                .body("id", is(1))
                .body("name", is("aa"));

    }
}
