package roomescape;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminWaitingAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("관리자는 모든 예약 대기 목록을 순번과 함께 조회한다")
    void getWaitings() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("확정자");
        insertWaiting("첫대기", themeSlotId);
        insertWaiting("둘대기", themeSlotId);

        RestAssured.given().log().all()
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("find { it.name == '첫대기' }.waitingOrder", equalTo(1))
                .body("find { it.name == '둘대기' }.waitingOrder", equalTo(2));
    }

    @Test
    @DisplayName("관리자는 예약 대기를 소유자 헤더 없이 취소한다")
    void deleteWaiting() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("확정자");
        long waitingId = insertWaiting("관리자취소대기", themeSlotId);

        RestAssured.given().log().all()
                .when().delete("/admin/waitings/{waitingId}", waitingId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("find { it.name == '관리자취소대기' }", nullValue());
    }

    private long createReservedThemeSlotWithConfirmedReservation(String name) {
        long themeSlotId = insertThemeSlot(LocalDate.now().plusDays(30));
        jdbcTemplate.update("""
                        INSERT INTO reservation (name, status, theme_slot_id)
                        VALUES (?, 'CONFIRMED', ?)
                        """,
                name,
                themeSlotId
        );
        return themeSlotId;
    }

    private long insertThemeSlot(LocalDate date) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
                    VALUES (?, ?, ?, true)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, 4L);
            ps.setObject(2, date);
            ps.setLong(3, 6L);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long insertWaiting(String name, long themeSlotId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO waiting (member_name, date, time_id, theme_id)
                    SELECT ?, date, time_id, theme_id
                    FROM theme_slot
                    WHERE id = ?
                    """, new String[]{"id"});
            ps.setString(1, name);
            ps.setLong(2, themeSlotId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

}
