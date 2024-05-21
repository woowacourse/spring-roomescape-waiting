package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.service.TokenProvider;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.util.ControllerTest;

import java.time.format.DateTimeFormatter;

import static roomescape.fixture.MemberFixture.getMemberTacan;

class WaitingReservationControllerTest extends ControllerTest {

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    ReservationRepository reservationRepository;

    String token;

    @BeforeEach
    void beforeEach() {
        token = tokenProvider.createAccessToken(getMemberTacan().getEmail());
    }

    @Test
    @DisplayName("예약이 존재하는 경우에도 사용자가 다르면 예약이 된다")
    void waiting() {
        //given
        ReservationTime reservationTime = ReservationTimeFixture.get2PM();
        Theme theme2 = ThemeFixture.getTheme2();
        Reservation alreadBookedReservation = ReservationFixture.getNextMonthReservation(reservationTime, theme2);
        ReservationRequest reservationRequest = new ReservationRequest(
                alreadBookedReservation.getDate().format(DateTimeFormatter.ISO_DATE),
                alreadBookedReservation.getTime().getId(),
                alreadBookedReservation.getTheme().getId()
        );

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }
}
