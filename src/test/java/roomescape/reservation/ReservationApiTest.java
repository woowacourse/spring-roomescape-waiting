package roomescape.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationApiTest {

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
    void 예약_조회() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_추가_및_삭제() {
        createTheme();
        createReservationTime("15:40");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        insertReservationSlot(futureDate, 1L, 1L);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", futureDate.toString());
        reservation.put("themeId", 1);
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].name", is("브라운"))
                .body("[0].date", is(futureDate.toString()))
                .body("[0].theme.id", is(1))
                .body("[0].theme.name", is("미술관의 밤"))
                .body("[0].time.id", is(1))
                .body("[0].time.startAt", is("15:40:00"));

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 존재하지_않는_예약_삭제_시_404를_반환한다() {
        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("code", is("MY_RESERVATION_NOT_FOUND"))
                .body("status", is(404));
    }

    @Test
    void DB_조회_API_전환() {
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail_url) VALUES (?, ?, ?, ?)",
                1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)", 1L, "15:40:00");
        insertReservation("브라운", LocalDate.parse("2023-08-05"), 1L, 1L);

        List<Map<String, Object>> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".");

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
        assertThat(((Map<?, ?>) reservations.get(0).get("time")).get("id")).isEqualTo(1);
    }

    @Test
    void DB_추가_삭제_API_전환() {
        createTheme();
        createReservationTime("10:00");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        insertReservationSlot(futureDate, 1L, 1L);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", futureDate.toString());
        reservation.put("themeId", 1);
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    void 이름이_다르면_예약을_삭제할_수_없다() {
        createTheme();
        createReservationTime("10:00");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        createReservation("브라운", futureDate.toString(), 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("name", "다른이름")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(404)
                .body("code", is("MY_RESERVATION_NOT_FOUND"))
                .body("status", is(404));

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 예약_삭제_시_첫_번째_대기가_예약으로_전환된다() {
        createTheme();
        createReservationTime("10:00");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        createReservation("브라운", futureDate.toString(), 1L, 1L);
        insertReservationWaiting(1L, "아루", "2026-06-07 12:00:00");
        insertReservationWaiting(1L, "도기", "2026-06-07 12:01:00");

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer reservationCount = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        String reservationName = jdbcTemplate.queryForObject("SELECT name FROM reservation", String.class);
        Integer waitingCount = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation_waiting", Integer.class);
        String waitingName = jdbcTemplate.queryForObject("SELECT name FROM reservation_waiting", String.class);

        assertThat(reservationCount).isEqualTo(1);
        assertThat(reservationName).isEqualTo("아루");
        assertThat(waitingCount).isEqualTo(1);
        assertThat(waitingName).isEqualTo("도기");
    }

    @Test
    void 예약과_시간_연결() {
        createTheme();
        createReservationTime("10:00");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        insertReservationSlot(futureDate, 1L, 1L);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", futureDate.toString());
        reservation.put("themeId", 1);
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("브라운"))
                .body("[0].theme.id", is(1))
                .body("[0].time.id", is(1))
                .body("[0].time.startAt", is("10:00:00"));
    }

    @Test
    void 예약_가능_시간_조회() {
        createTheme();
        createReservationTime("10:00");
        createReservationTime("11:00");
        LocalDate futureDate = LocalDate.now().plusDays(1);

        createReservation("브라운", futureDate.toString(), 1L, 1L);
        insertReservationSlot(futureDate, 1L, 2L);

        RestAssured.given().log().all()
                .queryParam("date", futureDate.toString())
                .when().get("/themes/1/times/available")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].startAt", is("11:00:00"));
    }

    @Test
    void 예약_추가_시_themeId가_없으면_400을_반환한다() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", futureDate.toString());
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"))
                .body("status", is(400));
    }

    @Test
    void 예약_추가_시_날짜_형식이_잘못되면_400을_반환한다() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026/05/20");
        reservation.put("themeId", 1);
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"))
                .body("status", is(400));
    }

    @Test
    void 예약_추가_시_themeId_타입이_잘못되면_400을_반환한다() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", futureDate.toString());
        reservation.put("themeId", "wrong");
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"))
                .body("status", is(400));
    }

    @Test
    void 예약_가능_시간_조회_시_날짜_형식이_잘못되면_400을_반환한다() {
        createTheme();

        RestAssured.given().log().all()
                .queryParam("date", "2026/05/20")
                .when().get("/themes/1/times/available")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"))
                .body("status", is(400));
    }

    @Test
    void 인기_테마_조회() {
        LocalDate today = LocalDate.now();

        createTheme("미술관의 밤");
        createTheme("심해 연구소");
        createTheme("폐병원 탈출");

        createReservationTime("10:00");
        createReservationTime("11:00");
        createReservationTime("12:00");
        insertReservation("쿠다", today.minusDays(1), 1L, 1L);
        insertReservation("아루", today.minusDays(2), 1L, 1L);
        insertReservation("도기", today.minusDays(3), 1L, 1L);

        insertReservation("포비", today.minusDays(1), 2L, 2L);
        insertReservation("솔라", today.minusDays(2), 2L, 2L);

        insertReservation("레오", today.minusDays(1), 3L, 3L);
        insertReservation("오래된예약", today.minusDays(10), 3L, 3L);

        RestAssured.given().log().all()
                .queryParam("period", 7)
                .queryParam("limit", 2)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("미술관의 밤"))
                .body("[1].name", is("심해 연구소"));
    }

    private void createTheme() {
        createTheme("미술관의 밤");
    }

    private void createTheme(final String name) {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", "추리 테마");
        theme.put("thumbnailUrl", "https://example.com/theme.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    private void createReservationTime(final String startAt) {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", startAt);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/reservation-times")
                .then().log().all()
                .statusCode(201);
    }

    private void createReservation(final String name, final String date, final Long themeId, final Long timeId) {
        insertReservationSlot(LocalDate.parse(date), themeId, timeId);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", name);
        reservation.put("date", date);
        reservation.put("themeId", themeId);
        reservation.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    private void insertReservation(final String name, final LocalDate date, final Long themeId, final Long timeId) {
        Long slotId = insertReservationSlot(date, themeId, timeId);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, slot_id) VALUES (?, ?)",
                name,
                slotId
        );
    }

    private Long insertReservationSlot(final LocalDate date, final Long themeId, final Long timeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)",
                java.sql.Date.valueOf(date),
                themeId,
                timeId
        );

        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE date = ? AND theme_id = ? AND time_id = ?",
                Long.class,
                java.sql.Date.valueOf(date),
                themeId,
                timeId
        );
    }

    private void insertReservationWaiting(final Long slotId, final String name, final String requestedAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (slot_id, name, requested_at) VALUES (?, ?, ?)",
                slotId,
                name,
                requestedAt
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
