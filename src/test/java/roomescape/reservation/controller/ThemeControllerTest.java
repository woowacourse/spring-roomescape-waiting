package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.utils.JdbcTemplateUtils;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
        String sql = "insert into member (name, email, password, role) values ('어드민', 'admin@woowa.com', '12341234', 'ADMIN')";
        jdbcTemplate.update(sql);
    }

    @DisplayName("테마를 추가한다.")
    @Test
    void test1() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마1");
        params.put("thumbnail", "www.m.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마를 가져온다")
    @Test
    void test2() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마1");
        params.put("thumbnail", "www.m.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("해당 테마를 삭제한다")
    @Test
    void test3() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마1");
        params.put("thumbnail", "www.m.com");

        int themeId = addTheme(params);

        RestAssured.given().log().all()
                .when().delete("/themes/" + themeId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("없는 테마를 삭제하면 NOT FOUND 반환")
    @Test
    void test4() {
        int notFoundStatusCode = 404;

        RestAssured.given().log().all()
                .when().delete("/themes/0")
                .then().log().all()
                .statusCode(notFoundStatusCode);
    }

    @DisplayName("사용 중인 테마가 있다면 삭제를 하면 409 CONFLICT를 반환한다.")
    @Test
    void test5() {
        int themeId = addTheme(Map.of("name", "테마1", "description", "테마1", "thumbnail", "www.m.com"));
        int timeId = addReservationTime("10:00");
        addReservation(timeId, themeId);
        int conflictStatusCode = 409;

        RestAssured.given().log().all()
                .when().delete("/themes/" + themeId)
                .then().log().all()
                .statusCode(conflictStatusCode);
    }

    @DisplayName("가장 인기있는 테마를 가져온다.")
    @Test
    void test6() {
        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    private void addReservation(int timeId, int themeId) {
        String tokenValue = getAdminLoginTokenValue();

        Map<String, Object> reservationParams = Map.of(
                "date", LocalDate.now().plusDays(1L),
                "timeId", timeId,
                "themeId", themeId
        );

        RestAssured.given().log().all()
                .cookie("token", tokenValue)
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then();
    }

    private String getAdminLoginTokenValue() {
        Map<String, String> adminLoginParams = Map.of("email", "admin@woowa.com", "password", "12341234");
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(adminLoginParams)
                .when().post("/login")
                .then()
                .extract().cookie("token");
    }

    private int addReservationTime(final String timeValue) {
        Map<String, String> timeParams = Map.of("startAt", timeValue);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().extract().path("id");
    }

    private int addTheme(final Map<String, Object> themeParams) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/themes")
                .then().extract().path("id");
    }
}
