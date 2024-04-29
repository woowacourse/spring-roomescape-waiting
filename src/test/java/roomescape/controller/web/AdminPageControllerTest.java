package roomescape.controller.web;

import static roomescape.TokenTestFixture.ADMIN_TOKEN;
import static roomescape.TokenTestFixture.USER_TOKEN;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AdminPageControllerTest {

    @DisplayName("성공: 관리자가 /admin 페이지 접속 -> 200")
    @Test
    void getAdminPage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().get("/admin")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 일반사용자가 /admin 페이지 접속 -> 401")
    @Test
    void getAdminPage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .when().get("/admin")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: 관리자가 /admin/reservation 페이지 접속 -> 200")
    @Test
    void getReservationPage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().get("/admin/reservation")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 일반사용자가 /admin/reservation 페이지 접속 -> 401")
    @Test
    void getReservationPage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .when().get("/admin/reservation")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: 관리자가 /admin/time 페이지 접속 -> 200")
    @Test
    void getReservationTimePage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().get("/admin/time")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 일반사용자가 /admin/time 페이지 접속 -> 401")
    @Test
    void getReservationTimePage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .when().get("/admin/time")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("성공: /admin/theme 페이지 응답 -> 200")
    @Test
    void getThemePage_Admin_Ok() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .when().get("/admin/theme")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: /admin/theme 페이지 응답 -> 401")
    @Test
    void getThemePage_User_Unauthorized() {
        RestAssured.given().log().all()
            .cookie("token", USER_TOKEN)
            .when().get("/admin/theme")
            .then().log().all()
            .statusCode(401);
    }
}
