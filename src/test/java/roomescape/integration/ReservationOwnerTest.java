package roomescape.integration;

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
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;

@SpringWebTest
public class ReservationOwnerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @DisplayName("이름을 header로 넘겨 자신의 예약을 삭제한다.")
    @Test
    void deleteMyReservationById_success() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
        );

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

    @DisplayName("예약 삭제 시, id에 해당하는 예약이 없으면 예외가 발생한다.")
    @Test
    void deleteMyReservationById_id_x() {
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예약 삭제 시, 자신의 예약이 아니면 예외가 발생한다.")
    @Test
    void deleteMyReservationById__not_owner() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
                );


        //when & then
        RestAssured.given().log().all()
                .header("Authorization", "pobi")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("예약 삭제 시, 이미 지난 시간이면 예외가 발생한다.")
    @Test
    void deleteMyReservationById_expired() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 4, 5)), 1L, 1L
        );


        //when & then
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("이름을 header로 넘겨서, 예약을 변경한다.")
    @Test
    void updateMyReservation_success() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(11, 0))
        );


        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
        );

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

    @DisplayName("예약 변경 시, 변경하려는 예약이 존재하지 않으면 예외가 발생한다.")
    @Test
    void updateMyReservation_id_x() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-05-10");

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예약 변경 시, 자신의 예약이 아니면 예외가 발생한다.")
    @Test
    void updateMyReservation_not_owner() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
        );

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-05-10");

        //when & then
        RestAssured.given().log().all()
                .header("Authorization", "pobi")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("예약 변경 시, name 헤더가 없으면 예외가 발생한다.")
    @Test
    void updateMyReservation_without_authorization() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
        );

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-05-10");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예약 변경 시, 변경 대상이 이미 지난 예약이면 예외가 발생한다.")
    @Test
    void updateMyReservation_expired_original() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 4, 5)), 1L, 1L
        );

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-05-10");

        //when & then
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("예약 변경 시, 변경하려는 시간이 이미 지났으면 예외가 발생한다.")
    @Test
    void updateMyReservation_expired_to() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
        );

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-04-10");

        //when & then
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("예약 변경 시, 날짜와 timeId가 모두 null이면 예외가 발생한다.")
    @Test
    void updateMyReservation__both_empty() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
        );


        //when & then
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(new HashMap<>())
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약 변경 시, 변경하려는 예약이 기존의 다른 예약과 겹치면 예외가 발생한다.")
    @Test
    void updateMyReservation_duplicate() {
        //given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(LocalDate.of(2026, 5, 5)), 1L, 1L
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id,  theme_id) VALUES (?, ?, ?, ?)",
                "pobi", Date.valueOf(LocalDate.of(2026, 5, 6)), 1L, 1L
        );

        //when & then
        Map<String, Object> paramsWithDate = new HashMap<>();
        paramsWithDate.put("date", "2026-05-06");

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(paramsWithDate)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(409);
    }
}
