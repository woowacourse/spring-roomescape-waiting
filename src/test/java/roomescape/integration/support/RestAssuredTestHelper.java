package roomescape.integration.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservationWaiting.controller.dto.ReservationWaitingRequest;
import roomescape.theme.controller.dto.ThemeRequest;
import roomescape.time.controller.dto.ReservationTimeRequest;

public class RestAssuredTestHelper {

    public static void createReservationTime(String startAt) {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.parse(startAt));
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/times")
                .then().statusCode(201);
    }

    public static void createTheme(String name, String description, String thumbnailUrl) {
        ThemeRequest request = new ThemeRequest(name, description, thumbnailUrl);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/themes")
                .then().statusCode(201);
    }

    public static Long createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationRequest request = new ReservationRequest(name, date, timeId, themeId);
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    public static void createReservationWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationWaitingRequest request = new ReservationWaitingRequest(name, date, timeId, themeId);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations-waitings")
                .then().statusCode(201);
    }
}
