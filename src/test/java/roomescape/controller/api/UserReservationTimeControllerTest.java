package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserReservationTimeControllerTest {

    private String userToken;

    @BeforeEach
    void login() {
        LoginRequest user = new LoginRequest("user@a.com", "123a!");

        userToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(user)
            .when().post("/login")
            .then().extract().cookie("token");
    }

    @DisplayName("성공: 날짜, 테마 ID로부터 예약 시간 및 가능 여부 반환")
    @Test
    void findAllWithAvailability() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
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
