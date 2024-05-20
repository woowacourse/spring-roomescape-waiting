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
import roomescape.controller.dto.CreateThemeRequest;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminThemeControllerTest {

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

    @DisplayName("성공: 테마 생성 -> 201")
    @Test
    void save() {
        CreateThemeRequest request = new CreateThemeRequest("t1", "d1", "https://test.com/test.jpg");

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(201)
            .body("id", is(1))
            .body("name", is("t1"))
            .body("description", is("d1"))
            .body("thumbnail", is("https://test.com/test.jpg"));
    }

    @DisplayName("성공: 테마 삭제 -> 204")
    @Test
    void delete() {
        jdbcTemplate.update("""
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('t1', 'd1', 'https://test.com/test1.jpg'),
            ('t2', 'd2', 'https://test.com/test2.jpg'),
            ('t3', 'd3', 'https://test.com/test3.jpg');
            """);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().delete("/admin/themes/2")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 3));
    }

    @DisplayName("실패: 잘못된 포맷으로 테마 생성 -> 400")
    @Test
    void save_IllegalTheme() {
        CreateThemeRequest request = new CreateThemeRequest("theme4", "desc4", "hello.jpg");

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(400)
            .body("message", containsString("올바른 URL 형식이 아닙니다."));
    }

    @DisplayName("실패: 중복 테마 추가 -> 400")
    @Test
    void save_Duplicate() {
        jdbcTemplate.update(
            "INSERT INTO theme(name, description, thumbnail) VALUES ('t1', 'd1', 'https://test.com/test.jpg')");

        CreateThemeRequest request = new CreateThemeRequest("t1", "d2", "https://test2.com/test.jpg");

        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(400)
            .body("message", is("같은 이름의 테마가 이미 존재합니다."));
    }

    @DisplayName("실패: 예약에서 사용되는 테마 삭제 -> 400")
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
            .when().delete("/admin/themes/1")
            .then().log().all()
            .statusCode(400)
            .body("message", is("해당 테마를 사용하는 예약이 존재하여 삭제할 수 없습니다."));
    }
}
