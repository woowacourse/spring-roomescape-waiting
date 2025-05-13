package roomescape.integrate.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.dto.reservationtime.ReservationTimeResponseDto;
import roomescape.dto.theme.ThemeResponseDto;

public class RequestFixture {

    public void reqeustSignup(String name, String email, String password) {
        Map<String, String> signupParam = Map.of("name", name, "email", email, "password", password);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupParam)
                .when().post("/auth/signup")
                .then()
                .statusCode(201);
    }

    public Map<String, String> requestLogin(String email, String password) {
        Map<String, String> loginParam = Map.of("email", email, "password", password);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParam)
                .when().post("/auth/login")
                .then().log().all()
                .extract().cookies();
    }

    public long requestAddTime(String startAt) {
        Map<String, String> timeParam = Map.of("startAt", startAt);

        ReservationTimeResponseDto reservationTimeResponseDto = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeParam)
                .when().post("/times")
                .then()
                .statusCode(201)
                .extract().body().as(ReservationTimeResponseDto.class);
        return reservationTimeResponseDto.id();
    }

    public long requestAddTheme(String name, String description, String thumbnail) {
        Map<String, String> themeParam = Map.of(
                "name", name,
                "description", description,
                "thumbnail", thumbnail
        );

        ThemeResponseDto themeResponseDto = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(themeParam)
                .when().post("/themes")
                .then()
                .statusCode(201)
                .extract().body().as(ThemeResponseDto.class);

        return themeResponseDto.id();
    }

    public long requestAddReservation(String name, String date, long timeId, long themeId,
                                      Map<String, String> cookies) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", name);
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        ReservationResponseDto reservationResponseDto = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservation)
                .when().post("/reservations")
                .then()
                .statusCode(201)
                .extract().body().as(ReservationResponseDto.class);

        return reservationResponseDto.id();
    }
}
