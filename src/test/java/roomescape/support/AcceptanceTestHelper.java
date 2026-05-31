package roomescape.support;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.Map;

public class AcceptanceTestHelper {

    public static Long createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public static Long createTheme(String name, String description, String thumbnailUrl) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "description", description,
                        "thumbnailUrl", thumbnailUrl
                ))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public static Long createReservation(String name, String date, Long timeId, Long themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "date", date,
                        "timeId", timeId,
                        "themeId", themeId
                ))
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public static Long createWaiting(String name, String date, Long timeId, Long themeId, int expectedOrderIndex) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "date", date,
                        "timeId", timeId,
                        "themeId", themeId
                ))
                .when().post("/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("orderIndex", is(expectedOrderIndex))
                .extract().jsonPath().getLong("id");
    }

    public static ValidatableResponse cancelReservation(Long reservationId, String name) {
        return RestAssured.given().log().all()
                .when().delete("/user/reservations/" + reservationId + "?name=" + name)
                .then().log().all();
    }

    public static ValidatableResponse findMyReservations(String name) {
        return RestAssured.given().log().all()
                .when().get("/user/reservations?name=" + name)
                .then().log().all();
    }
}
