package roomescape.acceptance.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.api.dto.request.ReservationRequest;
import roomescape.controller.api.dto.response.ReservationResponse;
import roomescape.controller.api.dto.response.ReservationTimeResponse;
import roomescape.controller.api.dto.response.ThemeResponse;

import static roomescape.acceptance.step.ReservationTimeStep.예약_시간_생성;
import static roomescape.acceptance.step.ThemeStep.테마_생성;

public class WaitingStep {
    public static void 대기_취소(final String token, final long waitingId) {
        //@formatter:off
        RestAssured.given().cookie(token).contentType(ContentType.JSON)
                .when().delete("/waitings/"+waitingId)
                .then().assertThat().statusCode(204);
        //@formatter:on
    }

    public static ReservationResponse 대기_생성(final String date, final String theme, final String time, final String token) {
        final ThemeResponse themeResponse = 테마_생성(theme);
        final ReservationTimeResponse reservationTimeResponse = 예약_시간_생성(time);
        return 대기_생성(date, themeResponse.id(), reservationTimeResponse.id(), token);
    }

    public static ReservationResponse 대기_생성(final String date, final long themeId, final long timeId, final String token) {
        final ReservationRequest request = new ReservationRequest(
                date,
                timeId,
                themeId
        );

        //@formatter:off
        return RestAssured.given().cookie(token).body(request).contentType(ContentType.JSON)
                .when().post("/waitings")
                .then().assertThat().statusCode(201).extract().as(ReservationResponse.class);
        //@formatter:on
    }
}
