package roomescape.reservation.controller;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
public class ReservationOwnerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    private Long setupDefaultReservation(LocalDate date) {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "brown", Date.valueOf(date), 1L, 1L
        );
        return jdbcTemplate.queryForObject("SELECT id FROM reservation LIMIT 1", Long.class);
    }

    @Test
    @DisplayName("이름을 header로 넘겨 자신의 예약을 삭제한다.")
    void deleteMyReservationById_success() {
        Long reservationId = setupDefaultReservation(LocalDate.now().plusDays(7));

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE id = ?)",
                Boolean.class,
                reservationId)).isFalse();
    }

    @Test
    @DisplayName("예약 삭제 시, id에 해당하는 예약이 없으면 예외가 발생한다.")
    void deleteMyReservationById_id_x() {
        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("예약 삭제 시, 자신의 예약이 아니면 예외가 발생한다.")
    void deleteMyReservationById_not_owner() {
        Long reservationId = setupDefaultReservation(LocalDate.of(2026, 5, 5));

        RestAssured.given().log().all()
                .header("Authorization", "pobi")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("예약 삭제 시, 이미 지난 시간이면 예외가 발생한다.")
    void deleteMyReservationById_expired() {
        Long reservationId = setupDefaultReservation(LocalDate.of(2026, 4, 5));

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("이름을 header로 넘겨서, 예약을 변경한다.")
    void updateMyReservation_success() {
        Long reservationId = setupDefaultReservation(LocalDate.now().plusDays(7));
        createReservationTime("11:00");

        ReservationUpdateRequest paramsWithDate = new ReservationUpdateRequest(LocalDate.now().plusDays(14), null);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(paramsWithDate)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT reservation_date FROM reservation WHERE id = ?",
                Date.class,
                reservationId).toLocalDate()).isEqualTo(LocalDate.now().plusDays(14));

        ReservationUpdateRequest paramsWithTimeId = new ReservationUpdateRequest(null, 2L);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(paramsWithTimeId)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT time_id FROM reservation WHERE id = ?",
                Long.class,
                reservationId)).isEqualTo(2L);
    }

    @Test
    @DisplayName("예약 변경 시, 변경하려는 예약이 존재하지 않으면 예외가 발생한다.")
    void updateMyReservation_id_x() {
        ReservationUpdateRequest params = new ReservationUpdateRequest(LocalDate.of(2026, 5, 10), null);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("예약 변경 시, 자신의 예약이 아니면 예외가 발생한다.")
    void updateMyReservation_not_owner() {
        Long reservationId = setupDefaultReservation(LocalDate.of(2026, 5, 5));

        ReservationUpdateRequest params = new ReservationUpdateRequest(LocalDate.of(2026, 5, 10), null);

        RestAssured.given().log().all()
                .header("Authorization", "pobi")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("예약 변경 시, name 헤더가 없으면 예외가 발생한다.")
    void updateMyReservation_without_authorization() {
        Long reservationId = setupDefaultReservation(LocalDate.of(2026, 5, 5));

        ReservationUpdateRequest params = new ReservationUpdateRequest(LocalDate.of(2026, 5, 10), null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("예약 변경 시, 변경 대상이 이미 지난 예약이면 예외가 발생한다.")
    void updateMyReservation_expired_original() {
        Long reservationId = setupDefaultReservation(LocalDate.of(2026, 4, 5));

        ReservationUpdateRequest params = new ReservationUpdateRequest(LocalDate.of(2026, 5, 10), null);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("예약 변경 시, 변경하려는 시간이 이미 지났으면 예외가 발생한다.")
    void updateMyReservation_expired_to() {
        Long reservationId = setupDefaultReservation(LocalDate.of(2026, 5, 5));

        ReservationUpdateRequest params = new ReservationUpdateRequest(LocalDate.of(2026, 4, 10), null);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("예약 변경 시, 날짜와 timeId가 모두 null이면 예외가 발생한다.")
    void updateMyReservation_both_empty() {
        Long reservationId = setupDefaultReservation(LocalDate.of(2026, 5, 5));

        Map<String, Object> emptyParams = new HashMap<>(); // DTO로는 둘 다 null인 객체 생성이 불가능(생성자에서 예외 발생). 따라서 이 테스트는 Map 유지

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(emptyParams)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 변경 시, 변경하려는 예약이 기존의 다른 예약과 겹치면 예외가 발생한다.")
    void updateMyReservation_duplicate() {
        Long reservationId = setupDefaultReservation(LocalDate.now().plusDays(7));
        createReservation("pobi", LocalDate.now().plusDays(8), 1L, 1L);

        ReservationUpdateRequest paramsWithDate = new ReservationUpdateRequest(LocalDate.now().plusDays(8), null);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(paramsWithDate)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(409);
    }
}
