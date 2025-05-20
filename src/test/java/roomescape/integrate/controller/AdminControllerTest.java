package roomescape.integrate.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.dto.admin.AdminReservationAddDto;
import roomescape.integrate.IntegrationTest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class AdminControllerTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("drop all objects");
    }

    @Test
    void 어드민_페이지에_접근시_권한이_부족하면_403이_발생한다() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login2@email.com", "password",
                "password");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then()
                .statusCode(201);

        Map<String, String> loginParam = Map.of("email", "token-login2@email.com", "password", "password");
        String header = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParam)
                .when().post("/auth/login")
                .then().log().all()
                .extract().header("Set-Cookie");

        String token = header.split("token=")[1];

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/admin")
                .then()
                .statusCode(403);
    }

    @Test
    void 어드민_권한을_가지면_어드민_페이지에_접근할_수_있다() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login2@email.com", "password",
                "password");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then()
                .statusCode(201);

        String sql = "UPDATE member SET role = ? WHERE name = ?";
        jdbcTemplate.update(sql, "ADMIN", "투다");

        Map<String, String> loginParam = Map.of("email", "token-login2@email.com", "password", "password");
        String header = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParam)
                .when().post("/auth/login")
                .then().log().all()
                .extract().header("Set-Cookie");

        String token = header.split("token=")[1];

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/admin")
                .then()
                .statusCode(200);
    }

    @Test
    void 어드민은_예약을_임의로_추가할_수_있다() {
        Map<String, String> signupParam = Map.of("name", "투다", "email", "token-login2@email.com", "password",
                "password");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then()
                .statusCode(201);

        String sql = "UPDATE member SET role = ? WHERE name = ?";
        jdbcTemplate.update(sql, "ADMIN", "투다");

        Map<String, String> loginParam = Map.of("email", "token-login2@email.com", "password", "password");
        String header = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParam)
                .when().post("/auth/login")
                .then().log().all()
                .extract().header("Set-Cookie");

        Map<String, String> timeParam = new HashMap<>();
        LocalTime afterTime = LocalTime.of(23, 59);
        timeParam.put("startAt", afterTime.toString());

        Map<String, String> themeParam = new HashMap<>();
        themeParam.put("name", "테마 명");
        themeParam.put("description", "description");
        themeParam.put("thumbnail", "thumbnail");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParam)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParam)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        String token = header.split("token=")[1];

        AdminReservationAddDto adminReservationAddDto = new AdminReservationAddDto(LocalDate.now().plusDays(2L), 1L, 1L,
                1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(adminReservationAddDto)
                .when().post("/admin/reservations")
                .then()
                .statusCode(201);
    }
}

