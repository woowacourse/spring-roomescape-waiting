package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.e2e.support.DatabaseHelper;
import roomescape.e2e.support.SpringWebTest;

@SpringWebTest
class ReservationWaitingE2ETest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @DisplayName("예약 대기 신청에 성공하면 201을 반환한다.")
    @Test
    void createReservationWaitingTest() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");
        createReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "pobi");
        body.put("date", LocalDate.of(2026, 5, 5));
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservation-waitings")
                .then().statusCode(201);
    }

    private void createReservationTime(String startAt) {
        Map<String, Object> reservationTime = new HashMap<>();
        reservationTime.put("startAt", startAt);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/admin/times")
                .then().statusCode(201);
    }

    private void createTheme(String name, String description, String thumbnailUrl) {
        Map<String, Object> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", description);
        theme.put("thumbnailUrl", thumbnailUrl);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().statusCode(201);
    }

    private void createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", name);
        reservation.put("date", date.toString());
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().statusCode(201);
    }

    @DisplayName("예약 대기 삭제에 성공하면 204를 반환한다.")
    @Test
    void deleteReservationWaitingTest() {
        //given
        createReservationTime("10:00");
        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");
        createReservation("brown", LocalDate.of(2026, 5, 30), 1L, 1L);
        createReservationWaiting("gump", LocalDate.of(2026, 5, 30), 1L, 1L);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "gump")
                .when().delete("/reservation-waitings/1")
                .then().statusCode(204);
    }

    private void createReservationWaiting(String name, LocalDate date, long timeId, long themeId) {
        Map<String, Object> reservationWaiting = new HashMap<>();
        reservationWaiting.put("name", name);
        reservationWaiting.put("date", date.toString());
        reservationWaiting.put("timeId", timeId);
        reservationWaiting.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationWaiting)
                .when().post("/reservation-waitings")
                .then().statusCode(201);
    }
}
