package roomescape.presentation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerIntTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        RestAssured.port = port;
        resetH2TableIds(jdbcTemplate);
    }

    @DisplayName("회원가입 성공")
    @Test
    void registerMemberTest() {
        int beforeSize = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member", Integer.class);

        Map<String, String> params = new HashMap<>();
        params.put("name", "user");
        params.put("email", "user@email.com");
        params.put("password", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().log().all()
                .statusCode(201)
                .body("id", is(beforeSize + 1));
    }
}
