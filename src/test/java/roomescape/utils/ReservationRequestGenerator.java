package roomescape.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import roomescape.core.dto.reservation.MemberReservationRequest;

public class ReservationRequestGenerator {
    private static final String ACCESS_TOKEN;

    static {
        ACCESS_TOKEN = AccessTokenGenerator.generate();
    }

    public static void generateWithTimeAndTheme(final Long timeId, final Long themeId) {
        MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().format(DateTimeFormatter.ISO_DATE), timeId, themeId);

        RestAssured.given().log().all()
                .cookies("token", ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
