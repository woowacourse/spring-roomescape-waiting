package roomescape.acceptance.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.api.dto.request.ReservationTimeRequest;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.domain.reservation.ReservationTime;
import roomescape.fixture.ReservationTimeFixture;

public class ReservationTimeStep {
    public static ReservationTimeResponse 예약_시간_생성() {
        final ReservationTime reservationTime = ReservationTimeFixture.getDomain();
        final ReservationTimeRequest request = new ReservationTimeRequest(
                reservationTime.getStartAtAsString()
        );

        //@formatter:off
        return RestAssured.given().body(request).contentType(ContentType.JSON)
                .when().post("/times")
                .then().assertThat().statusCode(201).extract().as(ReservationTimeResponse.class);
        //@formatter:on


    }
}
