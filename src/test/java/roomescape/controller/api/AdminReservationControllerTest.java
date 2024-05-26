package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateReservationRequest;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminReservationControllerTest {

    private String adminToken;
    private String userToken;

    @BeforeEach
    void login() {
        LoginRequest admin = new LoginRequest("admin@a.com", "123a!");
        LoginRequest user = new LoginRequest("user@a.com", "123a!");

        adminToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(admin)
            .when().post("/login")
            .then().extract().cookie("token");

        userToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(user)
            .when().post("/login")
            .then().extract().cookie("token");
    }

    @DisplayName("성공: 예약 추가 -> 201")
    @Test
    void save() {
        CreateReservationRequest request = new CreateReservationRequest(1L,
            "2026-06-06", 1L, 1L);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/reservations")
            .then().log().all()
            .statusCode(201);
    }

    @DisplayName("성공: 예약 삭제 -> 204")
    @Test
    void delete() {
        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().delete("/admin/reservations/3")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .body("id", contains(1, 2, 4, 5));
    }

    @DisplayName("실패: 일반 유저가 예약 삭제 -> 401")
    @Test
    void delete_ByUnauthorizedUser() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().delete("/admin/reservations/3")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: 전체 예약 조회 -> 200")
    @Test
    void findAll() {
        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2, 3, 4, 5));
    }

    @DisplayName("실패: 일반 유저가 전체 예약 조회 -> 401")
    @Test
    void findAll_ByUnauthorizedUser() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(401);
    }
}
