package roomescape.e2e;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class ReservationOwnerE2ETest extends E2ETest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @DisplayName("Authorization 헤더의 이름으로 자신의 예약을 삭제한다.")
    @Test
    void deleteMyReservationById_success() {
        //given
        createReservationTime(LocalTime.of(10, 0));
        createTheme();
        createReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        //when & then
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE id = ?)",
	                Boolean.class,
	                1L)).isFalse();
    }

    @DisplayName("예약 삭제 시 첫 번째 대기가 예약으로 승격되고 남은 대기의 순번이 재정렬된다.")
    @Test
    void deleteMyReservationById_promotes_first_waiting() {
        //given
        createReservationTime(LocalTime.of(10, 0));
        createTheme();
        createReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        createReservationWaiting("pobi", LocalDate.of(2026, 5, 5), 1L, 1L);
        createReservationWaiting("gump", LocalDate.of(2026, 5, 5), 1L, 1L);

        //when
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        //then
        assertThat(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE name = ? AND reservation_date = ? AND time_id = ? AND theme_id = ?)",
                Boolean.class,
                "pobi",
                Date.valueOf(LocalDate.of(2026, 5, 5)),
                1L,
                1L
        )).isTrue();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_waiting WHERE reservation_date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                Date.valueOf(LocalDate.of(2026, 5, 5)),
                1L,
                1L
        )).isEqualTo(1L);

        RestAssured.given().log().all()
                .when().get("/reservations?name=gump")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("waiting"))
                .body("[0].waitingOrder", is(1));
    }

    @DisplayName("Authorization 헤더의 이름으로 자신의 예약을 변경한다.")
    @Test
    void updateMyReservation_success() {
        //given
        createReservationTime(LocalTime.of(10, 0));
        createReservationTime(LocalTime.of(11, 0));
        createTheme();
        createReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        //when & then
        Map<String, Object> paramsWithDate = new HashMap<>();
        paramsWithDate.put("date", "2026-05-10");

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(paramsWithDate)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(204);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT reservation_date FROM reservation WHERE id = ?",
                Date.class,
                1L).toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 10));

        Map<String, Object> paramsWithTimeId = new HashMap<>();
        paramsWithTimeId.put("timeId", 2L);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(paramsWithTimeId)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(204);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT time_id FROM reservation WHERE id = ?",
                Long.class,
                1L)).isEqualTo(2L);
    }

    private void createReservationTime(LocalTime time) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(time)
        );
    }

    private void createTheme() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );
    }

    private void createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, Date.valueOf(date), timeId, themeId
        );
    }

    private void createReservationWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, Date.valueOf(date), timeId, themeId
        );
    }
}
