package roomescape.reservation.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class ReservationApiFixture {

    private ReservationApiFixture() {
    }

    public static Integer createReservationWithToken(String token, Integer dateId, Integer timeId,
        Integer themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RequestSpecification request = RestAssured.given().log().all()
            .contentType(ContentType.JSON);

        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, token);
        }

        return request.body(params)
            .when().post("/member/reservations")
            .then().log().all()
            .statusCode(200)
            .extract()
            .path("id");
    }


    public static void cancelReservationWithToken(String token, Integer reservationId) {
        Map<String, String> params = new HashMap<>();
        RequestSpecification request = RestAssured.given().log().all()
            .contentType(ContentType.JSON);

        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, token);
        }

        request.body(params)
            .when().patch("/member/reservations/" + reservationId + "/cancel")
            .then().log().all()
            .statusCode(200);
    }
}
