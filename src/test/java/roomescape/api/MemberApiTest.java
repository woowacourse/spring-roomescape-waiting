package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MemberApiTest {

    private static final Map<String, Object> MEMBER_BODY = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public MemberApiTest(
            @LocalServerPort final int port,
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeAll
    static void beforeAll() {
        MEMBER_BODY.put("email", "admin@email.com");
        MEMBER_BODY.put("password", "password");
        MEMBER_BODY.put("name", "어드민");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
    }

    @DisplayName("post 요청을 보내면 201 created를 응답한다.")
    @Test
    void post() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(MEMBER_BODY)
                .when().post("/members")
                .then().log().all().statusCode(201);
    }

    @DisplayName("이미 존재하는 이메일로 post 요청을 보내면 409 conflict를 응답한다.")
    @Test
    void post1() {
        // given
        givenCreateMember();

        // when & then
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(MEMBER_BODY)
                .when().post("/members")
                .then().log().all().statusCode(409);
    }

    @DisplayName("get 요청을 보내면 200과 member를 모두 조회하여 응답한다.")
    @Test
    void get() {
        // given
        givenCreateMember();

        // when & then
        RestAssured.given().port(port).log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("get 요청을 보내면 200과 member가 없다면 빈 컬렉션을 응답한다.")
    @Test
    void get1() {
        RestAssured.given().port(port).log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }


    private void givenCreateMember() {
        RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(MEMBER_BODY)
                .when().post("/members")
                .then().statusCode(201);
    }
}
