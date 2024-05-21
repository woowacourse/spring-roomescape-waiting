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
class MemberAcceptanceTest {
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
    @DisplayName("멤버 생성 API")
    void member_generate_API() {
        // given
        Map<String, String> member = Map.of("name", "aa", "email", "aa@aa.aa", "password", "aa");

        // when & then
        RestAssured
                .given().contentType(ContentType.JSON).body(member)
                .when().log().all().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED)
                .log().all();
    }

    @Test
    @DisplayName("멤버 조회 API")
    void member_inquiry_API() {
        // given
        Map<String, String> member1 = Map.of("name", "aa2", "email", "aa22@aa.aa", "password", "aa");
        Map<String, String> member2 = Map.of("name", "aa3", "email", "aa33@aa.aa", "password", "aa");
        Map<String, String> member3 = Map.of("name", "aa4", "email", "aa44@aa.aa", "password", "aa");

        RestAssured
                .given().contentType(ContentType.JSON).body(member1)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED);

        RestAssured
                .given().contentType(ContentType.JSON).body(member2)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED);

        RestAssured
                .given().contentType(ContentType.JSON).body(member3)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED);

        // when & then
        RestAssured
                .given()
                .when().get("/members")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(3));
    }

    @Test
    @DisplayName("멤버 삭제 API")
    void delete_member_API() {
        // given
        Map<String, String> member1 = Map.of("name", "aa2", "email", "aa55@aa.aa", "password", "aa");
        RestAssured
                .given().contentType(ContentType.JSON).body(member1)
                .when().post("/members")
                .then().statusCode(HttpStatus.SC_CREATED);

        // when
        RestAssured
                .given()
                .when().delete("/members/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // then
        RestAssured
                .given()
                .when().get("/members")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(0));
    }
}
