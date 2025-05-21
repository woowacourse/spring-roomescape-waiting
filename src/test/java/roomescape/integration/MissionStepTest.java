package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
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
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.ui.reservation.api.ReservationApiController;
import roomescape.reservation.ui.reservation.dto.ReservationResponse;
import roomescape.support.util.DateUtil;
import roomescape.support.util.SqlFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql"})
public class MissionStepTest {
    
    private static final String COOKIE_NAME = "token";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReservationApiController reservationApiController;

    @Test
    void 일단계() {
        // given
        insertDummyDataExceptReservation();
        final String token = extractTokenOfAdminLoginMember();

        // when & then
        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 이단계() {
        // given
        insertDummyDatas();
        final String token = extractTokenOfAdminLoginMember();

        // when & then
        RestAssured.given().log().all()
                .cookie(COOKIE_NAME, token)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType("application/json")
                .body("size()", is(8));
    }

    @Test
    void 삼단계() {
        // given
        insertDummyDataExceptReservation();
        final Map<String, Object> params = createDummyReservationParams();
        final String token = extractTokenOfAdminLoginMember();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .cookie(COOKIE_NAME, token)
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
                    () -> assertThat(connection.getCatalog()).isEqualTo("DATABASE"),
                    () -> assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null)
                            .next()).isTrue()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 오단계() {
        // given
        insertDummyDataExceptReservation();

        // when & then
        jdbcTemplate.update("INSERT INTO RESERVATION (MEMBER_ID, DATE, TIME_ID, THEME_ID) VALUES (?, ?, ?, ?)", 1, DateUtil.tomorrow().toString(), 1, 1);

        final List<ReservationResponse> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        final Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM RESERVATION", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    void 육단계() {
        // given
        insertDummyDataExceptReservation();
        final Map<String, Object> params = createDummyReservationParams();
        final String token = extractTokenOfAdminLoginMember();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(COOKIE_NAME, token)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        final Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM RESERVATION", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        final Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM RESERVATION", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    void 칠단계() {
        // given
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:30");

        // when & then
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
        // given
        insertDummyDataExceptReservation();
        final Map<String, Object> params = createDummyReservationParams();
        final String token = extractTokenOfAdminLoginMember();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(COOKIE_NAME, token)
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
        // given & when
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationApiController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        // then
        assertThat(isJdbcTemplateInjected).isFalse();
    }

    private void insertDummyData(final String sql) {
        jdbcTemplate.update(sql);
    }

    private void insertDummyDataExceptReservation() {
        insertDummyData(SqlFixture.INSERT_MEMBERS);
        insertDummyData(SqlFixture.INSERT_RESERVATION_TIMES);
        insertDummyData(SqlFixture.INSERT_THEMES);
    }

    private void insertDummyDatas() {
        SqlFixture.INSERT_ALL.forEach(jdbcTemplate::update);
    }

    private Map<String, Object> createDummyReservationParams() {
        final Map<String, Object> params = new HashMap<>();
        params.put("memberId", "1");
        params.put("date", DateUtil.tomorrow().toString());
        params.put("timeId", "1");
        params.put("themeId", "1");
        return params;
    }

    private String extractTokenOfAdminLoginMember() {
        final Map<String, String> loginParams = new HashMap<>();
        loginParams.put("email", "admin1@email.com");
        loginParams.put("password", "adminpw1");

        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract();

        String token = response.cookie(COOKIE_NAME);
        if (token == null) {
            throw new IllegalStateException("로그인 응답에서 토큰 쿠키를 찾을 수 없습니다.");
        }

        return token;
    }
}
