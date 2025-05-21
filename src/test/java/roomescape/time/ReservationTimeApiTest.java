package roomescape.time;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Sql("/test-time-data.sql")
public class ReservationTimeApiTest {

    @DisplayName("시간 조회를 성공할 경우 200을 반환한다.")
    @Test
    void testFindAll() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(3));
    }
}
