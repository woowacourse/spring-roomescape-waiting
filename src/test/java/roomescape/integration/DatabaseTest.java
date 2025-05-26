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
import roomescape.presentation.dto.response.ReservationResponse;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DatabaseTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, Object> reservation = new HashMap<>();
    private static final Map<String, Object> authOfMember = new HashMap<>();

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        setUpReservation();
        setUpAuthOfMember();
    }

    private void setUpReservation() {
        reservation.put("date", "2025-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
    }

    private void setUpAuthOfMember() {
        authOfMember.put("email", "test@email.com");
        authOfMember.put("password", "pass1");
    }

    @Test
    void 데이터베이스_연결을_검증한다() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 방탈출_예약_목록을_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)",
                "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "테마1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO member (name, role, email, password) VALUES (?, ?, ?, ?)",
                "브라운", "USER", "test@email.com", "pass1");
        jdbcTemplate.update("INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES (?, ?, ?, ?, ?)",
                "2025-08-05", 1, 1, 1, "RESERVED");

        List<ReservationResponse> response = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);

        assertThat(response.size()).isEqualTo(count);
    }

    @Test
    void 방탈출_예약_목록을_생성_조회_삭제한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)",
                "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "테마1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO member (name, role, email, password) VALUES (?, ?, ?, ?)",
                "브라운", "USER", "test@email.com", "pass1");
        String token = givenMemberLoginToken();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation WHERE status = 'RESERVED'", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    private String givenMemberLoginToken() {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(authOfMember)
                .when().post("/login")
                .then()
                .extract().response().cookie("token");
    }
}
