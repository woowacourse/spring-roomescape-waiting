package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;
import static roomescape.TokenTestFixture.ADMIN_TOKEN;
import static roomescape.TokenTestFixture.USER_TOKEN;

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
class AdminReservationControllerTest {

    @DisplayName("성공: 예약 삭제 -> 204")
    @Test
    void delete() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().delete("/admin/reservations/3")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().get("/admin/reservations")
            .then().log().all()
            .body("id", contains(1, 2, 4));
    }

    @DisplayName("실패: 일반 유저가 예약 삭제 -> 401")
    @Test
    void delete_ByUnauthorizedUser() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .when().delete("/admin/reservations/3")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: 전체 예약 조회 -> 200")
    @Test
    void findAll() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2, 3, 4));
    }

    @DisplayName("실패: 일반 유저가 전체 예약 조회 -> 401")
    @Test
    void findAll_ByUnauthorizedUser() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(401);
    }
}
