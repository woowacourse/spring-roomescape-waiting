package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.dto.ReservationResponse;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DataSourceTest {

    private static final Map<String, String> RESERVATION_BODY = new HashMap<>();
    private static final Map<String, String> TIME_BODY = new HashMap<>();
    private static final Map<String, String> THEME_BODY = new HashMap<>();
    private static final Map<String, String> MEMBER_BODY = new HashMap<>();
    private static final Map<String, Object> AUTH_BODY = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public DataSourceTest(
            @Autowired final JdbcTemplate jdbcTemplate,
            @LocalServerPort final int port
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.port = port;
    }

    @BeforeAll
    static void beforeAll() {
        RESERVATION_BODY.put("date", "2026-08-05");
        RESERVATION_BODY.put("timeId", "1");
        RESERVATION_BODY.put("themeId", "1");

        TIME_BODY.put("startAt", "10:00");

        THEME_BODY.put("name", "theme");
        THEME_BODY.put("description", "dest");
        THEME_BODY.put("thumbnail", "thumbnail");

        MEMBER_BODY.put("name", "브라운");
        MEMBER_BODY.put("email", "asd@email.com");
        MEMBER_BODY.put("password", "pass");

        AUTH_BODY.put("email", "asd@email.com");
        AUTH_BODY.put("password", "pass");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM RESERVATION");
        jdbcTemplate.update("DELETE FROM RESERVATION_TIME");
        jdbcTemplate.update("DELETE FROM THEME");
        jdbcTemplate.update("DELETE FROM MEMBER");

        jdbcTemplate.update("ALTER TABLE RESERVATION ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE RESERVATION_TIME ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE THEME ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
    }

    @DisplayName("데이터베이스가 존재하고, 예약 테이블이 존재하는지 검증")
    @Test
    void 사단계() {
        try (final Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("데이터베이스에 예약 하나 추가 후 예약 조회 API를 통해 조회한 예약 수와 데이터베이스 쿼리를 통해 조회한 예약 수가 같은지 비교")
    @Test
    void 오단계() {
        // given
        jdbcTemplate.update("INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)", "name", "email@email.com", "password", "MEMBER");
        jdbcTemplate.update("INSERT INTO THEME (name, description, thumbnail) VALUES (?, ?, ?)", "name", "dest", "thumb");
        jdbcTemplate.update("INSERT INTO RESERVATION_TIME (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO RESERVATION (date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)", "2023-08-05",
                "1", "1", "1");
        final Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        // when
        final List<ReservationResponse> reservations = RestAssured.given().port(port).log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        // then
        assertThat(reservations.size()).isEqualTo(count);
    }

    @DisplayName("reservation 삽입, 삭제 검증")
    @Test
    void 육단계() {
        // given & when
        givenCreateTheme();
        givenCreateReservationTime();
        givenCreateMember();
        final Cookie cookie = givenAuthCookie();
        givenCreateReservation(cookie);

        // then
        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        // given & when & then
        RestAssured.given().port(port).log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @DisplayName("time과 reservation 연결 테스트")
    @Test
    void 팔단계() {
        // given
        givenCreateTheme();
        givenCreateReservationTime();
        givenCreateMember();
        final Cookie cookie = givenAuthCookie();
        givenCreateReservation(cookie);

        // when & then
        RestAssured.given().port(port).log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    private void givenCreateReservationTime() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(TIME_BODY)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    private void givenCreateReservation(final Cookie cookie) {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(RESERVATION_BODY)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    private void givenCreateTheme() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(THEME_BODY)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    private void givenCreateMember() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(MEMBER_BODY)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    private Cookie givenAuthCookie() {
        return RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(AUTH_BODY)
                .when().post("/login")
                .then()
                .extract().detailedCookie("token");
    }
}
