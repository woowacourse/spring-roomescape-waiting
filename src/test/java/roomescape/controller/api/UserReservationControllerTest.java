package roomescape.controller.api;

import static org.hamcrest.Matchers.is;
import static roomescape.TokenTestFixture.USER_TOKEN;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateReservationRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserReservationControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("성공: 예약 저장 -> 201")
    @Test
    void save() {
        CreateReservationRequest request = new CreateReservationRequest(
            2L, "2060-01-01", 1L, 1L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", USER_TOKEN)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("id", is(5))
            .body("memberName", is("사용자"))
            .body("date", is("2060-01-01"))
            .body("time", is("10:00"))
            .body("themeName", is("theme1"));
    }


    @DisplayName("실패: 존재하지 않는 time id 예약 -> 400")
    @Test
    void save_TimeIdNotFound() {
        CreateReservationRequest request = new CreateReservationRequest(
            2L, "2060-01-01", 3L, 1L);

        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("message", is("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    @DisplayName("실패: 존재하지 않는 theme id 예약 -> 400")
    @Test
    void save_ThemeIdNotFound() {
        CreateReservationRequest request = new CreateReservationRequest(
            2L, "2060-01-01", 1L, 4L);

        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("message", is("입력한 테마 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    @DisplayName("실패: 중복 예약 -> 400")
    @Test
    void save_Duplication() {
        jdbcTemplate.update("""
            INSERT INTO reservation (member_id, date, time_id, theme_id)
            VALUES (1, '2060-01-01', 1, 1)
            """);

        CreateReservationRequest request = new CreateReservationRequest(
            2L, "2060-01-01", 1L, 1L);

        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("실패: 과거 시간 예약 -> 400")
    @Test
    void save_PastTime() {
        CreateReservationRequest request = new CreateReservationRequest(1L, "2000-01-01", 1L, 1L);

        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }
}
