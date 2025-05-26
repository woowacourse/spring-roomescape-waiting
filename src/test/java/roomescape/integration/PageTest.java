package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.common.BaseTest;

import java.util.HashMap;
import java.util.Map;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PageTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, String> member = new HashMap<>();
    private static final Map<String, Object> authOfMember = new HashMap<>();

    private static final Map<String, String> admin = new HashMap<>();
    private static final Map<String, Object> authOfAdmin = new HashMap<>();

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        setUpMemberAndLogin();
        setUpAdminAndLogin();
    }

    private void setUpMemberAndLogin() {
        member.put("name", "브라운");
        member.put("email", "test@email.com");
        member.put("password", "pass1");

        authOfMember.put("email", "test@email.com");
        authOfMember.put("password", "pass1");
    }

    private void setUpAdminAndLogin() {
        admin.put("name", "듀이");
        admin.put("email", "test2@email.com");
        admin.put("password", "pass2");

        authOfAdmin.put("email", "test2@email.com");
        authOfAdmin.put("password", "pass2");
    }

    @Test
    void 메인_페이지를_응답한다() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 로그인_페이지를_응답한다() {
        RestAssured.given().log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 관리자_페이지를_응답한다() {
        givenCreatedAdmin();
        String token = givenAdminLoginToken();

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 로그인하지_않고_관리자_페이지에_접근하면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 사용자가_관리자_페이지에_접근하면_예외를_응답한다() {
        givenCreatedMember();
        String token = givenMemberLoginToken();

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 관리자_예약관리_페이지를_응답한다() {
        givenCreatedAdmin();
        String token = givenAdminLoginToken();

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 관리자_시간관리_페이지를_응답한다() {
        givenCreatedAdmin();
        String token = givenAdminLoginToken();

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 관리자_테마관리_페이지를_응답한다() {
        givenCreatedAdmin();
        String token = givenAdminLoginToken();

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 관리자_예약대기관리_페이지를_응답한다() {
        givenCreatedAdmin();
        String token = givenAdminLoginToken();

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/waiting")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 사용자_예약페이지를_응답한다() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 사용자_내예약목록_페이지를_응답한다() {
        RestAssured.given().log().all()
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private void givenCreatedMember() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(member)
                .when().post("/members");
    }

    private String givenMemberLoginToken() {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(authOfMember)
                .when().post("/login")
                .then()
                .extract().response().cookie("token");
    }

    private void givenCreatedAdmin() {
        jdbcTemplate.update("INSERT INTO member (name, role, email, password) VALUES (?, ?, ?, ?)",
                admin.get("name"), "ADMIN", admin.get("email"), admin.get("password"));
    }

    private String givenAdminLoginToken() {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(authOfAdmin)
                .when().post("/login")
                .then()
                .extract().response().cookie("token");
    }
}
