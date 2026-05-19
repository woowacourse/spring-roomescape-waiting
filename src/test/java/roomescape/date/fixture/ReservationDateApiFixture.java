package roomescape.date.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class ReservationDateApiFixture {

    private ReservationDateApiFixture() {
    }

    public static Integer createReservationDate(String token, String date) {
        Map<String, String> params = new HashMap<>();
        params.put("date", date);

        return RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/dates")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("id");
    }

    public static void updateDateStatus(String token, Integer futureDateId, boolean isActive) {
        Map<String, Object> updateActive = new HashMap<>();
        updateActive.put("isActive", isActive);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(updateActive)
                .when().patch("/admin/dates/" + futureDateId + "/status")
                .then().log().all()
                .statusCode(200);
    }

}
