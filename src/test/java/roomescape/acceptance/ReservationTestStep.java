package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;

public class ReservationTestStep {
    public static Long postClientReservation(String token, String date, Long timeId, Long themeId, int expectedHttpCode) {
        Map<?, ?> requestBody = Map.of("date", date, "timeId", timeId, "themeId", themeId);

        Response response = RestAssured.given().log().all()
                .cookies("token", token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        if (expectedHttpCode == 201) {
            return response.jsonPath().getLong("id");
        }

        return null;
    }

    public static Long postAdminReservation(String token, String date, Long memberId, Long timeId, Long themeId, int expectedHttpCode) {
        Map<?, ?> requestBody = Map.of("date", date, "memberId", memberId, "timeId", timeId, "themeId", themeId);

        Response response = RestAssured.given().log().all()
                .cookies("token", token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        if (expectedHttpCode == 201) {
            return response.jsonPath().getLong("id");
        }

        return null;
    }

    public static void getReservations(int expectedHttpCode, int expectedReservationsSize) {
        Response response = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<?> reservationResponses = response.as(List.class);

        assertThat(reservationResponses).hasSize(expectedReservationsSize);
    }

    public static void deleteReservation(Long reservationId, int expectedHttpCode) {
        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(expectedHttpCode);
    }
}
