package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.service.TokenProvider;
import roomescape.fixture.ReservationFixture;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.util.ControllerTest;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static roomescape.fixture.MemberFixture.getMemberTacan;

class ReservationControllerWaitingTest extends ControllerTest {

    @Autowired
    TokenProvider tokenProvider;

    String token;

    @BeforeEach
    void beforeEach() {
        token = tokenProvider.createAccessToken(getMemberTacan().getEmail());
    }

    @Test
    @DisplayName("예약이 존재하는 경우에도 사용자가 다르면 예약이 된다")
    void waiting() {
        //given
        Reservation bookedMemberReservation = ReservationFixture.getBookedReservation();
        ReservationSlot alreadBookedReservationSlot = bookedMemberReservation.getReservationSlot();
        ReservationRequest reservationRequest = new ReservationRequest(
                alreadBookedReservationSlot.getDate().format(DateTimeFormatter.ISO_DATE),
                alreadBookedReservationSlot.getTime().getId(),
                alreadBookedReservationSlot.getTheme().getId()
        );

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 대기인 예약을 삭제한다.")
    @Test
    void delete() {
        //given
        Reservation bookedMemberReservation = ReservationFixture.getBookedReservation();
        ReservationSlot bookedReservationSlot = bookedMemberReservation.getReservationSlot();
        ReservationRequest reservationRequest = new ReservationRequest(
                bookedReservationSlot.getDate().format(DateTimeFormatter.ISO_DATE),
                bookedReservationSlot.getTime().getId(),
                bookedReservationSlot.getTheme().getId()
        );

//        when & then
        String location = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .header("Location");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete(location)
                .then().log().all()
                .statusCode(204);
    }
}
