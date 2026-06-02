package roomescape.acceptance.assertion;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;

public class ReservationTimeAssertions {

    public static void checkAllReservationTimeSize(int expectedSize) {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }

    public static void checkAvailableReservation(String date, Long themeId, boolean expectedAvailable) {
        RestAssured.given().log().all()
                .queryParam("date", date)
                .queryParam("themeId", themeId)
                .when().get("/times/available")
                .then().log().all()
                .statusCode(200)
                .body("[0].available", is(expectedAvailable));
    }
}
