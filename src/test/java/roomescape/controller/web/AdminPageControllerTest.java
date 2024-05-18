package roomescape.controller.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(value = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminPageControllerTest {

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

    @DisplayName("성공: 관리자가 /admin 페이지 접속 -> 200")
    @Test
    void getAdminPage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 일반사용자가 /admin 페이지 접속 -> 401")
    @Test
    void getAdminPage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().get("/admin")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: 관리자가 /admin/reservation 페이지 접속 -> 200")
    @Test
    void getReservationPage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/reservation")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 일반사용자가 /admin/reservation 페이지 접속 -> 401")
    @Test
    void getReservationPage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().get("/admin/reservation")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: 관리자가 /admin/time 페이지 접속 -> 200")
    @Test
    void getReservationTimePage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/time")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 일반사용자가 /admin/time 페이지 접속 -> 401")
    @Test
    void getReservationTimePage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().get("/admin/time")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: /admin/theme 페이지 응답 -> 200")
    @Test
    void getThemePage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", adminToken)
            .when().get("/admin/theme")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: /admin/theme 페이지 응답 -> 401")
    @Test
    void getThemePage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().get("/admin/theme")
            .then().log().all()
            .statusCode(401);
    }
}
