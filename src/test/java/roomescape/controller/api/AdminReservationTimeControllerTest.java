package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
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
import roomescape.controller.dto.CreateTimeRequest;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminReservationTimeControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminToken;

    @BeforeEach
    void setUpToken() {
        jdbcTemplate.update(
            "INSERT INTO member(name, email, password, role) VALUES ('관리자', 'admin@a.com', '123a!', 'ADMIN');");

        LoginRequest admin = new LoginRequest("admin@a.com", "123a!");

        adminToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(admin)
            .when().post("/login")
            .then().extract().cookie("token");
    }

    @DisplayName("성공: 예약 시간 저장 -> 201")
    @Test
    void save() {
        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .body(new CreateTimeRequest("00:00"))
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(201)
            .body("id", is(1))
            .body("startAt", is("00:00"));
    }

    @DisplayName("성공: 예약 시간 삭제 -> 204")
    @Test
    void delete() {
        jdbcTemplate.update("""
            INSERT INTO reservation_time(start_at)
            VALUES ('10:00'),
                   ('23:00'),
                   ('23:30');
            """);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .when().delete("/admin/times/2")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 3));
    }

    @DisplayName("성공: 예약 시간 조회 -> 200")
    @Test
    void findAll() {
        jdbcTemplate.update("""
            INSERT INTO reservation_time(start_at)
            VALUES ('10:00'),
                   ('23:00');
            """);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2))
            .body("startAt", contains("10:00", "23:00"));
    }

    @DisplayName("실패: 잘못된 포맷의 예약 시간 저장 -> 400")
    @Test
    void save_IllegalTimeFormat() {
        CreateTimeRequest request = new CreateTimeRequest("24:00");

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(400)
            .body("message", containsString("HH:mm 형식으로 입력해 주세요."));
    }

    @DisplayName("예약이 존재하는 시간 삭제 -> 400")
    @Test
    void delete_ReservationExists() {
        jdbcTemplate.update("""
            INSERT INTO reservation_time(start_at) VALUES ('10:00');
            INSERT INTO theme(name, description, thumbnail) VALUES ('t1', 'd1', 'https://test.com/test.jpg');
            INSERT INTO reservation(member_id, reserved_date, created_at, time_id, theme_id, status)
            VALUES (1, '2060-01-01', '2024-01-01', 1, 1, 'RESERVED');
            """);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .when().delete("/admin/times/1")
            .then().log().all()
            .statusCode(400)
            .body("message", is("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다."));
    }

    @DisplayName("실패: 이미 존재하는 시간을 저장 -> 400")
    @Test
    void save_Duplicate() {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('10:00')");

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .body(new CreateTimeRequest("10:00"))
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(400)
            .body("message", is("이미 존재하는 시간은 추가할 수 없습니다."));
    }
}

