package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseE2ETest {

    protected static final String PASSWORD_PLAIN = "password";
    protected static final String PASSWORD_HASH =
            "$2a$10$LzNRNMIeDFJdLCa.esEa0.RW6uxlRb3JruT7QtWUHF.xAIJeDzDrC";

    @LocalServerPort
    private int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void baseSetUp() {
        RestAssured.port = port;
        truncateAll();
    }

    protected void truncateAll() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String table : List.of("orders", "promotion_outbox", "waitings", "reservations", "members", "stores", "themes", "times")) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
            jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH 1");
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    protected Long seedStore(String name) {
        jdbcTemplate.update("INSERT INTO stores(name) VALUES (?)", name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM stores WHERE name = ?", Long.class, name);
    }

    protected Long seedMember(String name, String email, String role) {
        return seedMember(name, email, role, null);
    }

    protected Long seedMember(String name, String email, String role, Long storeId) {
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role, store_id) VALUES (?, ?, ?, ?, ?)",
                name, email, PASSWORD_HASH, role, storeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM members WHERE email = ?", Long.class, email);
    }

    protected Long seedTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO times(start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM times WHERE start_at = ?", Long.class, startAt);
    }

    protected Long seedTheme(String name) {
        return seedTheme(name, 30000L);
    }

    protected Long seedTheme(String name, long price) {
        jdbcTemplate.update(
                "INSERT INTO themes(name, thumbnail_url, description, price) VALUES (?, ?, ?, ?)",
                name, "http://thumbnail/" + name, "설명", price);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM themes WHERE name = ?", Long.class, name);
    }

    protected Long seedReservation(Long memberId, Long timeId, Long themeId, Long storeId, LocalDate date) {
        jdbcTemplate.update(
                "INSERT INTO reservations(member_id, date, time_id, theme_id, store_id, status) "
                        + "VALUES (?, ?, ?, ?, ?, 'BOOKED')",
                memberId, date, timeId, themeId, storeId);
        return jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM reservations", Long.class);
    }

    protected Long seedWaiting(Long memberId, Long timeId, Long themeId, Long storeId, LocalDate date) {
        jdbcTemplate.update(
                "INSERT INTO waitings(member_id, date, time_id, theme_id, store_id) VALUES (?, ?, ?, ?, ?)",
                memberId, date, timeId, themeId, storeId);
        return jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM waitings", Long.class);
    }

    protected String loginAs(String email) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", PASSWORD_PLAIN))
                .when().post("/login")
                .then().statusCode(200)
                .extract().sessionId();
    }
}
