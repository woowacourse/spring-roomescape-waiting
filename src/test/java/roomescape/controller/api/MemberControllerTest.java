package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MemberControllerTest {

    @DisplayName("성공: 모든 회원 조회 -> 200")
    @Test
    void findAll() {
        RestAssured.given().log().all()
            .when().get("/members")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2));
    }
}
