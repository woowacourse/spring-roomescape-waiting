package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserReservationTimeControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String userToken;

    @BeforeEach
    void setUpToken() {
        jdbcTemplate.update(
            "INSERT INTO member(name, email, password, role) VALUES ('러너덕', 'user@a.com', '123a!', 'USER')");

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
        jdbcTemplate.update("""
            INSERT INTO reservation_time(start_at) VALUES ('10:00'), ('23:00');
            INSERT INTO theme(name, description, thumbnail) VALUES ('t1', 'd1', 'https://test.com/test.jpg');
            INSERT INTO reservation (member_id, reserved_date, time_id, theme_id, status)
            VALUES (1, '2060-01-01', 1, 1, 'RESERVED');
            """);

        RestAssured.given().log().all()
            .cookie("token", userToken)
            .queryParam("date", "2060-01-01")
            .queryParam("id", 1L)
            .when().get("/times/available")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2))
            .body("startAt", contains("10:00", "23:00"))
            .body("alreadyBooked", contains(true, false));
    }
}
