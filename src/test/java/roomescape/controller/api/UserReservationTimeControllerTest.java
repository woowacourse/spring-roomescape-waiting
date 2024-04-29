package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;
import static roomescape.TokenTestFixture.USER_TOKEN;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserReservationTimeControllerTest {

    @DisplayName("성공: 날짜, 테마 ID로부터 예약 시간 및 가능 여부 반환")
    @Test
    void findAllWithAvailability() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .queryParam("date", LocalDate.now().minusDays(1).toString())
            .queryParam("id", 1L)
            .when().get("/times/available")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2))
            .body("startAt", contains("10:00", "23:00"))
            .body("alreadyBooked", contains(true, false));
    }
}
