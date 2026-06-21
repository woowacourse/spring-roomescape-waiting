package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.controller.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminReservationControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 목록이 없으면 빈 배열을 응답한다")
    void respondEmptyArrayWhenReservationsDoNotExist() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 목록을 조회한다")
    void findReservations() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", futureDate, 1L, 1L);

        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(reservations).hasSize(count);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("생성한 예약을 관리자 목록에서 조회한다")
    void findCreatedReservationInAdminList() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "email", emailFromName("브라운"),
                        "date", futureDate,
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("관리자는 예약일 당일에도 예약 일정을 수정할 수 있다")
    void adminCanUpdateReservationScheduleOnReservationDate() {
        final String today = LocalDate.now().toString();
        final String tomorrow = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", today, 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", tomorrow,
                        "timeId", 2
                ))
                .when().put("/admin/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", is(1))
                .body("date", is(tomorrow))
                .body("time.id", is(2));

        Map<String, Object> updatedReservation = jdbcTemplate.queryForMap(
                """
                        SELECT s.reservation_date, s.time_id
                        FROM reservation r
                        JOIN reservation_slot s ON r.slot_id = s.id
                        WHERE r.id = ?
                        """,
                1L
        );
        assertThat(updatedReservation.get("RESERVATION_DATE").toString()).isEqualTo(tomorrow);
        assertThat(updatedReservation.get("TIME_ID")).isEqualTo(2L);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("관리자는 제약없이 예약을 취소할 수 있다")
    void adminCanCancelReservation() {
        final String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)", "링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", futureDate, 1L, 1L);

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 예약을 관리자가 취소하면 404를 응답한다")
    void respondNotFoundWhenAdminCancelsNonExistingReservation() {
        RestAssured.given().log().all()
                .when().delete("/admin/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("message", is("존재하지 않는 예약입니다."));
    }

    private void insertReservation(final String name, final String date, final long timeId, final long themeId) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation (customer_name, customer_email, slot_id, status) VALUES (?, ?, ?, ?)",
                name,
                emailFromName(name),
                slotId,
                "CONFIRMED"
        );
    }

    private String emailFromName(final String name) {
        return "customer" + Math.abs(name.hashCode()) + "@example.com";
    }

    private Long insertReservationSlot(final String date, final long timeId, final long themeId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)",
                    date,
                    timeId,
                    themeId
            );
        } catch (DuplicateKeyException ignored) {
        }
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE reservation_date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                timeId,
                themeId
        );
    }
}
