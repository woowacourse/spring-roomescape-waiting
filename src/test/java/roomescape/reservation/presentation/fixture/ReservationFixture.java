package roomescape.reservation.presentation.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ThemeRequest;
import roomescape.reservation.presentation.dto.WaitingRequest;

public class ReservationFixture {

    public AdminReservationRequest createAdminReservationRequest(LocalDate date, Long themeId, Long timeId,
                                                                 Long memberId) {
        return new AdminReservationRequest(date, themeId, timeId, memberId);
    }

    public ReservationRequest createReservationRequest(LocalDate date, Long themeId, Long timeId) {
        return new ReservationRequest(date, themeId, timeId);
    }

    public ReservationTimeRequest createReservationTimeRequest(LocalTime startAt) {
        return new ReservationTimeRequest(startAt);
    }

    public ThemeRequest createThemeRequest(String name, String description, String thumbnail) {
        return new ThemeRequest(name, description, thumbnail);
    }

    public WaitingRequest createWaitingRequest(final LocalDate date, final Long themeId, final Long timeId) {
        return new WaitingRequest(date, themeId, timeId);
    }

    public void createWaiting(LocalDate date, Long themeId, Long timeId, Map<String, String> cookies) {
        final WaitingRequest waitingRequest = createWaitingRequest(date, themeId, timeId);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(waitingRequest)
                .when().post("/reservations");
    }

    public void createReservation(LocalDate date, Long themeId, Long timeId, Map<String, String> cookies) {
        final ReservationRequest reservationRequest = createReservationRequest(date, themeId, timeId);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationRequest)
                .when().post("/reservations");
    }

    public void createReservationTime(LocalTime startAt, Map<String, String> cookies) {
        final ReservationTimeRequest reservationTimeRequest = createReservationTimeRequest(startAt);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeRequest)
                .when().post("/times");
    }

    public void createTheme(String name, String description, String thumbnail, Map<String, String> cookies) {
        final ThemeRequest themeRequest = createThemeRequest(name, description, thumbnail);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(themeRequest)
                .when().post("/themes");
    }
}
