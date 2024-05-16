package roomescape.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.http.MediaType;
import roomescape.core.dto.auth.TokenRequest;
import roomescape.core.dto.reservation.MemberReservationRequest;

public class ReservationRequestGenerator {
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN;

    static {
        ACCESS_TOKEN = RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
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
