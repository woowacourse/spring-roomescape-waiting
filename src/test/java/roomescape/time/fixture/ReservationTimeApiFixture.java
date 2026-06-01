package roomescape.time.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;

public class ReservationTimeApiFixture {

    private ReservationTimeApiFixture() {
    }

    public static Integer createReservationTime(String token, String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(200)
            .extract()
            .path("id");
    }

    public static void updateTimeStatus(String token, Integer timeId, boolean isActive) {
        Map<String, Object> updateActive = new HashMap<>();
        updateActive.put("isActive", isActive);

        RestAssured.given().log().all()
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(ContentType.JSON)
            .body(updateActive)
            .when().patch("/admin/times/" + timeId + "/status")
            .then().log().all()
            .statusCode(200);
    }

}
