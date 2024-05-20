package roomescape.acceptance.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.api.dto.request.ReservationRequest;
import roomescape.controller.api.dto.response.ReservationResponse;

public class ReservationStep {
    public static ReservationResponse 예약_생성(final String date, final long themeId, final long timeId, final String token) {
        final ReservationRequest request = new ReservationRequest(
                date,
                timeId,
                themeId
        );

        //@formatter:off
        return RestAssured.given().cookie(token).body(request).contentType(ContentType.JSON)
                   .when().post("/reservations")
                   .then().assertThat().statusCode(201).extract().as(ReservationResponse.class);
        //@formatter:on
    }
}
