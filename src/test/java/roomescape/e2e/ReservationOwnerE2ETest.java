package roomescape.e2e;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.e2e.support.DatabaseHelper;
import roomescape.e2e.support.SpringWebTest;

@SpringWebTest
public class ReservationOwnerE2ETest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

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
}
