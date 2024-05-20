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
class AdminReservationControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUpTokens() {
        jdbcTemplate.update("""
            INSERT INTO member(name, email, password, role)
            VALUES ('관리자', 'admin@a.com', '123a!', 'ADMIN'),
                   ('사용자', 'user@a.com', '123a!', 'USER');
            """);

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

    @DisplayName("성공: 예약 삭제 가능, 다음 순위 예약대기는 자동 예약")
    @Test
    void delete() {
        jdbcTemplate.update("""
            INSERT INTO reservation_time(start_at) VALUES ('10:00');
            INSERT INTO theme(name, description, thumbnail) VALUES ('t1', 'd1', 'https://test.com/test.jpg');
            INSERT INTO reservation(member_id, reserved_date, created_at, time_id, theme_id, status)
            VALUES (1, '2060-01-01', '2024-01-01', 1, 1, 'RESERVED'),
                   (2, '2060-01-01', '2024-01-02', 1, 1, 'STANDBY'),
                   (1, '2060-01-02', '2024-01-03', 1, 1, 'RESERVED'),
                   (2, '2060-01-02', '2024-01-04', 1, 1, 'STANDBY');
            """);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().delete("/admin/reservations/1")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(2, 3));

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/reservations/standby")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(4));
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
        jdbcTemplate.update("""
            INSERT INTO reservation_time(start_at) VALUES ('10:00');
            INSERT INTO theme(name, description, thumbnail) VALUES ('t1', 'd1', 'https://test.com/test.jpg');
            INSERT INTO reservation(member_id, reserved_date, created_at, time_id, theme_id, status)
            VALUES (1, '2060-01-01', '2024-01-01', 1, 1, 'RESERVED'),
                   (2, '2060-01-02', '2024-01-01', 1, 1, 'RESERVED'),
                   (1, '2060-01-03', '2024-01-01', 1, 1, 'RESERVED');
            """);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2, 3));
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

    @DisplayName("성공: 전체 대기목록 조회 -> 200")
    @Test
    void findAllStandby() {
        jdbcTemplate.update("""
            INSERT INTO reservation_time(start_at) VALUES ('10:00');
            INSERT INTO theme(name, description, thumbnail) VALUES ('t1', 'd1', 'https://test.com/test.jpg');
            INSERT INTO reservation(member_id, reserved_date, created_at, time_id, theme_id, status)
            VALUES (1, '2060-01-01', '2024-01-01', 1, 1, 'RESERVED'),
                   (2, '2060-01-01', '2024-01-02', 1, 1, 'STANDBY'),
                   (1, '2060-01-03', '2024-01-01', 1, 1, 'RESERVED'),
                   (2, '2060-01-03', '2024-01-01', 1, 1, 'STANDBY');
            """);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/reservations/standby")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(2, 4));
    }
}
