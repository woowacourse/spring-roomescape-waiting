package roomescape.reservationslot;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
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
class ReservationSlotAdminApiTest {

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
    void 예약_슬롯_관리_API() {
        long themeId = insertTheme();
        long timeId = insertReservationTime();
        Map<String, Object> slot = new HashMap<>();
        slot.put("date", "2026-08-06");
        slot.put("themeId", themeId);
        slot.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/admin/reservation-slots")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("date", is("2026-08-06"))
                .body("theme.id", is((int) themeId))
                .body("time.id", is((int) timeId));

        RestAssured.given().log().all()
                .when().get("/admin/reservation-slots")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 같은_날짜_테마_시간의_예약_슬롯은_중복_생성할_수_없다() {
        long themeId = insertTheme();
        long timeId = insertReservationTime();
        Map<String, Object> slot = new HashMap<>();
        slot.put("date", "2026-08-06");
        slot.put("themeId", themeId);
        slot.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/admin/reservation-slots")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/admin/reservation-slots")
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESERVATION_SLOT_DUPLICATED"))
                .body("status", is(409));
    }

    @Test
    void 예약_슬롯_추가_시_날짜가_없으면_400을_반환한다() {
        Map<String, Object> slot = new HashMap<>();
        slot.put("themeId", insertTheme());
        slot.put("timeId", insertReservationTime());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/admin/reservation-slots")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"))
                .body("status", is(400));
    }

    private long insertTheme() {
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail_url) VALUES (1, '미술관의 밤', '추리 테마', 'https://example.com/theme.png')"
        );
        return 1L;
    }

    private long insertReservationTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00:00')");
        return 1L;
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
