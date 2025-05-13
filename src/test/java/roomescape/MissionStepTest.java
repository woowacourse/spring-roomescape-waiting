package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.AuthenticationService;
import roomescape.presentation.AuthenticationTokenCookie;
import roomescape.presentation.response.ReservationResponse;
import roomescape.presentation.rest.ReservationController;

@Sql(scripts = {"/test-schema.sql"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReservationController reservationController;
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void 일단계() {
        insertOneUser();
        var token = authenticationService.issueToken("email@email.com", "password");

        RestAssured.given().log().all()
            .cookie(AuthenticationTokenCookie.COOKIE_KEY, token)
            .when().get("/admin")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 이단계() {
        insertOneUser();
        var token = authenticationService.issueToken("email@email.com", "password");

        RestAssured.given().log().all()
            .cookie(AuthenticationTokenCookie.COOKIE_KEY, token)
            .when().get("/admin/reservation")
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType("application/json")
            .body("size()",
                is(0)); // 아직 생성 요청이 없으니 Controller에서 임의로 넣어준 Reservation 갯수 만큼 검증하거나 0개임을 확인하세요.
    }

    @Test
    void 삼단계() {
        Map<String, String> params = new HashMap<>();
        params.put("date", DateUtils.tomorrow().toString());
        params.put("timeId", "1");
        params.put("themeId", "1");

        insertOneUser();
        insertOneReservationTimeSlot();
        insertOneReservationTheme();
        var token = authenticationService.issueToken("email@email.com", "password");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .cookie(AuthenticationTokenCookie.COOKIE_KEY, token)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", is(1));

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .body("size()", is(1));

        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .body("size()", is(0));
    }

    @Test
    void 사단계() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertAll(
                () -> assertThat(connection).isNotNull(),
                () -> assertThat(connection.getCatalog()).isEqualTo("DATABASE-TEST"),
                () -> assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null)
                    .next()).isTrue()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 오단계() {
        insertOneUser();
        insertOneReservationTimeSlot();
        insertOneReservationTheme();
        jdbcTemplate.update("INSERT INTO reservation (user_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", 1,
            DateUtils.tomorrow().toString(), 1, 1);

        List<ReservationResponse> reservations = RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.OK.value()).extract()
            .jsonPath().getList(".", ReservationResponse.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation",
            Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    void 육단계() {
        Map<String, String> params = new HashMap<>();
        params.put("date", DateUtils.tomorrow().toString());
        params.put("timeId", "1");
        params.put("themeId", "1");

        insertOneUser();
        insertOneReservationTimeSlot();
        insertOneReservationTheme();
        var token = authenticationService.issueToken("email@email.com", "password");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(AuthenticationTokenCookie.COOKIE_KEY, token)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value());

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation",
            Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation",
            Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    void 칠단계() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/times")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
            .when().get("/times")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .body("size()", is(1));

        RestAssured.given().log().all()
            .when().delete("/times/1")
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 팔단계() {
        insertOneUser();
        insertOneReservationTimeSlot();
        insertOneReservationTheme();
        var token = authenticationService.issueToken("email@email.com", "password");

        Map<String, Object> params = new HashMap<>();
        params.put("date", DateUtils.tomorrow().toString());
        params.put("timeId", 1);
        params.put("themeId", "1");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie(AuthenticationTokenCookie.COOKIE_KEY, token)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .body("size()", is(1));
    }

    @Test
    void 구단계() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }

    private void insertOneUser() {
        jdbcTemplate.update("INSERT INTO users (id, name, role, email, password) VALUES (?, ?, ?, ?, ?)", 1, "admin", "ADMIN", "email@email.com", "password");
    }

    private void insertOneReservationTimeSlot() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
    }

    private void insertOneReservationTheme() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    }
}
