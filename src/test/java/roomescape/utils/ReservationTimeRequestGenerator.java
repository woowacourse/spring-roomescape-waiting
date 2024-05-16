package roomescape.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.MediaType;
import roomescape.core.dto.auth.TokenRequest;
import roomescape.core.dto.reservationtime.ReservationTimeRequest;

public class ReservationTimeRequestGenerator {
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

    public static void generateOneMinuteAfter() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(
                LocalTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));

        RestAssured.given().log().all()
                .cookies("token", ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    public static void generateTwoMinutesAfter() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(
                LocalTime.now().plusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm")));

        RestAssured.given().log().all()
                .cookies("token", ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }
}
