package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestTimeConfig.class)
public class MissionStepTest {

    @Test
    void 예약_조회() {
        String accessToken = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "testAdmin", "password", "test2"))
                .when().post("/api/login")
                .then().statusCode(200)
                .extract()
                .path("data.accessToken");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/reservations")
                .then().log().all()
                .statusCode(200)
                .body("data.size()", is(4));
    }
}
