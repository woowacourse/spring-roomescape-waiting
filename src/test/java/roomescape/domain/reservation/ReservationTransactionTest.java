package roomescape.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTransactionTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 승격_중_예외_발생_시_예약_삭제가_롤백된다() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("유저1", "2099-12-31", timeId, themeId);
        insertWaiting("대기자1", "2099-12-31", timeId, themeId);

        doThrow(new RuntimeException("승격 실패 시뮬레이션"))
                .when(reservationRepository).save(any(Reservation.class));

        RestAssured.given()
                .when().delete("/reservations/" + reservationId)
                .then()
                .statusCode(500);

        Integer reservationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Integer.class, reservationId
        );
        Integer waitingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE date = ?", Integer.class, "2099-12-31"
        );
        assertAll(
                () -> assertEquals(1, reservationCount, "승격 실패 시 예약 삭제도 롤백되어야 한다"),
                () -> assertEquals(1, waitingCount, "승격 실패 시 대기도 그대로 남아있어야 한다")
        );
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
                name, "설명", "https://example.com/image.jpg"
        );
        return jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private Long insertTime(String startAt, String finishAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
                startAt, finishAt
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt
        );
    }

    private Long insertReservation(String name, String date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId
        );
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
    }

    private Long insertWaiting(String name, String date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId
        );
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM waiting", Long.class);
    }
}