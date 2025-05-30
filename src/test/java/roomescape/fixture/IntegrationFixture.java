package roomescape.fixture;

import static org.hamcrest.Matchers.is;
import static roomescape.fixture.TestFixture.FUTURE_DATE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import roomescape.common.security.dto.request.LoginRequest;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;

public class IntegrationFixture {

    public static final String REGULAR_EMAIL = "regular@gmail.com";
    public static final String REGULAR2_EMAIL = "alice@gmail.com";
    public static final String ADMIN_EMAIL = "admin@gmail.com";
    public static final String PASSWORD = "password";
    public static final String FUTURE_DATE_TEXT = FUTURE_DATE.toString();
    public static final String TOKEN = "token";

    public static String loginAndGetAuthToken(final String email, final String password) {
        return RestAssured.given().log().all()
                .body(new LoginRequest(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie(TOKEN);
    }

    public static void createRegularReservation(final Long themeId) {
        String authToken = loginAndGetAuthToken(REGULAR_EMAIL, PASSWORD);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", FUTURE_DATE_TEXT);
        reservation.put("timeId", 1);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .cookie(TOKEN, authToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    public static void createTheme(final String name) {
        String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

        Map<String, String> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", "셜록 with Danny");
        theme.put("thumbnail", "image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .cookie(TOKEN, authToken)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    public static void createReservationTime() {
        String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

        Map<String, String> reservationTime = new HashMap<>();
        reservationTime.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .cookie(TOKEN, authToken)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    public static void findThemesBySize(final int size) {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(size));
    }

    public static ReservationResponse makeWaitingReservations() {
        createReservationTime();
        createTheme("추리");
        createRegularReservation(1L);

        String user2Token = loginAndGetAuthToken(REGULAR2_EMAIL, PASSWORD);
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", FUTURE_DATE_TEXT);
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        ReservationResponse reservationResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TOKEN, user2Token)
                .body(reservation)
                .when().post("/waiting-reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(ReservationResponse.class);
        return reservationResponse;
    }
}
