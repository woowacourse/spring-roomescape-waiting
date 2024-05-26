package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

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
import roomescape.controller.dto.CreateReservationRequest;
import roomescape.controller.dto.LoginRequest;
import roomescape.fixture.LoginRequestFixture;
import roomescape.fixture.ReservationRequestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserReservationControllerTest {

    private String userToken;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void login() {
        LoginRequest user = LoginRequestFixture.createUserRequest();

        userToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(user)
            .when().post("/login")
            .then().extract().cookie("token");
    }

    @DisplayName("성공: 예약 저장 -> 201")
    @Test
    void save() {
        CreateReservationRequest request = ReservationRequestFixture.create();

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", userToken)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("id", is(6))
            .body("memberName", is("사용자"))
            .body("date", is("2060-01-01"))
            .body("time", is("10:00"))
            .body("themeName", is("theme1"));
    }

    @DisplayName("실패: 존재하지 않는 time id 예약 -> 400")
    @Test
    void save_TimeIdNotFound() {
        CreateReservationRequest request = ReservationRequestFixture.createWithInvalidTimeId();

        RestAssured.given().log().all()
            .cookie("token", userToken)
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
        CreateReservationRequest request = ReservationRequestFixture.createWithInvalidThemeId();

        RestAssured.given().log().all()
            .cookie("token", userToken)
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

        CreateReservationRequest request = ReservationRequestFixture.create();

        RestAssured.given().log().all()
            .cookie("token", userToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("실패: 과거 시간 예약 -> 400")
    @Test
    void save_PastTime() {
        CreateReservationRequest request = ReservationRequestFixture.createWithPastTime();
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("성공: 나의 예약 목록 조회 -> 200")
    @Test
    void findMyReservations() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .contentType(ContentType.JSON)
            .when().get("/reservations-mine")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(2, 4, 5));
    }

    @DisplayName("성공: 예약 대기 요청 -> 200")
    @Test
    void saveWaiting() {
        CreateReservationRequest request = ReservationRequestFixture.create();

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", userToken)
            .body(request)
            .when().post("/reservations/waiting")
            .then().log().all()
            .statusCode(201)
            .body("id", is(6))
            .body("memberName", is("사용자"))
            .body("date", is("2060-01-01"))
            .body("time", is("10:00"))
            .body("themeName", is("theme1"));
    }

    @DisplayName("성공: 예약 대기 삭제 -> 204")
    @Test
    void delete() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().delete("/reservations-mine/5")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().get("/reservations-mine")
            .then().log().all()
            .body("id", contains(2, 4));
    }
}
