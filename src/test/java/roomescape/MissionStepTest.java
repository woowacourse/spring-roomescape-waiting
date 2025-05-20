package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.controller.ReservationController;
import roomescape.dto.LoginRequest;
import roomescape.dto.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationController reservationController;

    private String sessionId;

    @BeforeEach
    void setUp() {
        final LoginRequest loginRequest = new LoginRequest("admin@email.com", "1234");
        sessionId = RestAssured.given().contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");
    }

    @DisplayName("일단계")
    @Test
    void level1() {
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("이단계")
    @Test
    void level2() {
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @DisplayName("삼단계")
    @Test
    void level3() {
        Map<String, String> timeParams = Map.of(
                "startAt", "15:30"
        );

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0));

        Map<String, String> params = Map.of(
                "memberId", "1",
                "date", LocalDate.now().plusDays(1).toString(),
                "themeId", "1",
                "timeId", "1"
        );

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0));

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @DisplayName("사단계")
    @Test
    void level4() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("오단계")
    @Test
    void level5() {
        Map<String, String> timeParams = Map.of(
                "startAt", "15:30"
        );

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0));

        jdbcTemplate.update("INSERT INTO reservation (member_id, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                "1", "2023-08-05", 1, 1);

        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations).hasSize(count);
    }

    @DisplayName("육단계")
    @Test
    void level6() {
        Map<String, String> timeParams = Map.of(
                "startAt", "15:30"
        );

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0));

        Map<String, String> params = Map.of(
                "memberId", "1",
                "date", LocalDate.now().plusDays(1).toString(),
                "themeId", "1",
                "timeId", "1"
        );

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isPositive();

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isLessThan(count);
    }

    @DisplayName("칠단계")
    @Test
    void level7() {
        Map<String, String> params = Map.of(
                "startAt", "10:00"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));

        RestAssured.given().log().all()
                .when().delete("/times/4")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("팔단계")
    @Test
    void level8() {
        Map<String, String> timeParams = Map.of(
                "startAt", "15:30"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0));

        Map<String, String> params = Map.of(
                "memberId", "1",
                "date", LocalDate.now().plusDays(1).toString(),
                "themeId", "1",
                "timeId", "1"
        );

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }


    @DisplayName("구단계")
    @Test
    void level9() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
