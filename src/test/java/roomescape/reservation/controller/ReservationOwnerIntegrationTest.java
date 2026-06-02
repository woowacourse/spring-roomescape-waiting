package roomescape.reservation.controller;

import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Date;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
class ReservationOwnerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    private Long setupDefaultReservation(String name, LocalDate date) {
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");

        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, Date.valueOf(date), 1L, 1L
        );
        return jdbcTemplate.queryForObject("SELECT id FROM reservation WHERE name = ? LIMIT 1", Long.class, name);
    }

    @Test
    @DisplayName("인증 헤더가 없으면 예약 수정 시 401을 반환한다.")
    void updateMyReservation_MissingAuthHeader_Unauthorized() {
        Long reservationId = setupDefaultReservation("brown", LocalDate.now().plusDays(1));
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.now().plusDays(2), null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("예약 소유자가 불일치하면 403을 반환한다.")
    void updateMyReservation_MismatchOwner_Forbidden() {
        Long reservationId = setupDefaultReservation("brown", LocalDate.now().plusDays(1));
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.now().plusDays(2), null);

        RestAssured.given().log().all()
                .header("Authorization", "anotherUser")
                .contentType(ContentType.JSON)
                .body(request)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("예약 수정 시 date와 timeId가 모두 null이면 400을 반환한다.")
    void updateMyReservation_BothNullFields_BadRequest() {
        Long reservationId = setupDefaultReservation("brown", LocalDate.now().plusDays(1));

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body("{}")
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 수정 요청이 성공하면 204를 반환한다.")
    void updateMyReservation_Success() {
        Long reservationId = setupDefaultReservation("brown", LocalDate.now().plusDays(1));
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.now().plusDays(2), null);

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .contentType(ContentType.JSON)
                .body(request)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("인증 헤더가 없으면 예약 삭제 시 401을 반환한다.")
    void deleteMyReservation_MissingAuthHeader_Unauthorized() {
        Long reservationId = setupDefaultReservation("brown", LocalDate.now().plusDays(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("내 예약 삭제가 성공하면 204를 반환한다.")
    void deleteMyReservation_Success() {
        Long reservationId = setupDefaultReservation("brown", LocalDate.now().plusDays(1));

        RestAssured.given().log().all()
                .header("Authorization", "brown")
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }
}
