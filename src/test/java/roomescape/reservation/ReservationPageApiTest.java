package roomescape.reservation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Date;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationPageApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        clearTables();
    }

    @Test
    void 이름으로_내_예약_목록을_조회한다() {
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "10:00:00");
        createReservation("brown", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("reservationName", "brown")
                .when().get("/pages/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body(containsString("brown"))
                .body(containsString("미술관의 밤"))
                .body(containsString("10:00"));
    }

    @Test
    void 이름으로_조회한_내_예약을_취소한다() {
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "10:00:00");
        createReservation("brown", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .redirects().follow(false)
                .formParam("reservationName", "brown")
                .when().post("/pages/user/reservations/1/delete")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("/pages/user/reservations"))
                .header("Location", containsString("reservationName=brown"));

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(0);
    }

    @Test
    void 이름으로_조회한_내_예약의_날짜와_시간을_변경한다() {
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "10:00:00");
        createReservationTime(2L, "11:00:00");
        createReservation("brown", LocalDate.now().plusDays(1), 1L, 1L);

        LocalDate changedDate = LocalDate.now().plusDays(2);
        createReservationSlot(changedDate, 1L, 2L);

        RestAssured.given().log().all()
                .redirects().follow(false)
                .formParam("reservationName", "brown")
                .formParam("date", changedDate.toString())
                .formParam("timeId", 2)
                .when().post("/pages/user/reservations/1/update")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("/pages/user/reservations"))
                .header("Location", containsString("reservationName=brown"));

        String updatedDate = jdbcTemplate.queryForObject(
                """
                        SELECT rs.date
                        FROM reservation r
                        JOIN reservation_slot rs ON r.slot_id = rs.id
                        WHERE r.id = 1
                        """,
                String.class
        );
        Long updatedTimeId = jdbcTemplate.queryForObject(
                """
                        SELECT rs.time_id
                        FROM reservation r
                        JOIN reservation_slot rs ON r.slot_id = rs.id
                        WHERE r.id = 1
                        """,
                Long.class
        );

        org.assertj.core.api.Assertions.assertThat(updatedDate).isEqualTo(changedDate.toString());
        org.assertj.core.api.Assertions.assertThat(updatedTimeId).isEqualTo(2L);
    }

    @Test
    void 지난_예약_취소_시_에러코드와_함께_리다이렉트한다() {
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "00:00:00");
        createReservation("brown", LocalDate.now().minusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .redirects().follow(false)
                .formParam("reservationName", "brown")
                .when().post("/pages/user/reservations/1/delete")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("errorCode=PAST_RESERVATION_CANNOT_BE_CANCELLED"));
    }

    @Test
    void 이미_차있는_시간으로_변경하면_에러코드와_함께_리다이렉트한다() {
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "10:00:00");
        createReservationTime(2L, "11:00:00");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        createReservation("brown", reservationDate, 1L, 1L);
        createReservation("brown", reservationDate, 1L, 2L);

        RestAssured.given().log().all()
                .redirects().follow(false)
                .formParam("reservationName", "brown")
                .formParam("date", reservationDate.toString())
                .formParam("timeId", 2)
                .when().post("/pages/user/reservations/1/update")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("errorCode=RESERVATION_DUPLICATED"));
    }

    @Test
    void 예약_마감_시간에_다른_이름으로_대기를_신청한다() {
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "10:00:00");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        createReservation("brown", reservationDate, 1L, 1L);

        RestAssured.given().log().all()
                .redirects().follow(false)
                .formParam("name", "aru")
                .formParam("date", reservationDate.toString())
                .formParam("themeId", 1)
                .formParam("timeId", 1)
                .when().post("/pages/user/reservations/waitings")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("/pages/user/reservations"))
                .header("Location", containsString("reservationName=aru"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM reservation_waiting WHERE slot_id = 1 AND name = 'aru'",
                Integer.class
        );
        org.assertj.core.api.Assertions.assertThat(count).isOne();
    }

    @Test
    void 예약자는_자신의_예약에_대기_신청할_수_없다() {
        createTheme(1L, "미술관의 밤");
        createReservationTime(1L, "10:00:00");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        createReservation("brown", reservationDate, 1L, 1L);

        RestAssured.given().log().all()
                .redirects().follow(false)
                .formParam("name", "brown")
                .formParam("date", reservationDate.toString())
                .formParam("themeId", 1)
                .formParam("timeId", 1)
                .when().post("/pages/user/reservations/waitings")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("errorCode=RESERVATION_WAITING_DUPLICATED"));
    }

    private void createTheme(final long id, final String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail_url) VALUES (?, ?, ?, ?)",
                id,
                name,
                "추리 테마",
                "https://example.com/theme.png"
        );
    }

    private void createReservationTime(final long id, final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                id,
                startAt
        );
    }

    private void createReservation(
            final String name,
            final LocalDate date,
            final long themeId,
            final long timeId
    ) {
        Long slotId = createReservationSlot(date, themeId, timeId);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, slot_id) VALUES (?, ?)",
                name,
                slotId
        );
    }

    private Long createReservationSlot(
            final LocalDate date,
            final long themeId,
            final long timeId
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)",
                Date.valueOf(date),
                themeId,
                timeId
        );

        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE date = ? AND theme_id = ? AND time_id = ?",
                Long.class,
                Date.valueOf(date),
                themeId,
                timeId
        );
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
