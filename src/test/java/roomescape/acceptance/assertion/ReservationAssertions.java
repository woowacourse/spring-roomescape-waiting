package roomescape.acceptance.assertion;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;

public class ReservationAssertions {

    public static void readMyName(String name, int expectedSize, String reservationStatus) {
        RestAssured.given().log().all()
                .queryParam("name", name)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize))
                .body("[0].status", is(reservationStatus));
    }

    public static void checkAllReservationSize(int expectedSize) {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }
}
