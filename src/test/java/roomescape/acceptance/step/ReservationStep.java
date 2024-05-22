package roomescape.acceptance.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.api.dto.request.ReservationRequest;
import roomescape.controller.api.dto.response.ReservationResponse;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.controller.api.dto.response.ThemeResponse;

import static roomescape.acceptance.step.ReservationTimeStep.예약_시간_생성;
import static roomescape.acceptance.step.ThemeStep.테마_생성;

public class ReservationStep {

    public static ReservationResponse 예약_생성(final String date, final String theme, final String time, final String token) {
        final ThemeResponse themeResponse = 테마_생성(theme);
        final ReservationTimeResponse reservationTimeResponse = 예약_시간_생성(time);
        return 예약_생성(date, themeResponse.id(), reservationTimeResponse.id(), token);
    }

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

    public static void 예약_취소(final String token, final long reservationId) {
        //@formatter:off
        RestAssured.given().cookie(token).contentType(ContentType.JSON)
                .when().delete("/reservations/" + reservationId)
                .then().assertThat().statusCode(204);
        //@formatter:on
    }
}
