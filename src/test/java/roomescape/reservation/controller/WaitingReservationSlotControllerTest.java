package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.service.TokenProvider;
import roomescape.fixture.MemberReservationFixture;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.util.ControllerTest;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static roomescape.fixture.MemberFixture.getMemberTacan;

class WaitingReservationSlotControllerTest extends ControllerTest {

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    MemberReservationRepository memberReservationRepository;

    String token;

    @BeforeEach
    void beforeEach() {
        token = tokenProvider.createAccessToken(getMemberTacan().getEmail());
    }

    @Test
    @DisplayName("예약이 존재하는 경우에도 사용자가 다르면 예약이 된다")
    void waiting() {
        //given
        MemberReservation bookedMemberReservation = MemberReservationFixture.getBookedMemberReservation();
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
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 대기인 예약을 삭제한다.")
    @Test
    void delete() {
        //given
        MemberReservation bookedMemberReservation = MemberReservationFixture.getBookedMemberReservation();
        ReservationSlot bookedReservationSlot = bookedMemberReservation.getReservationSlot();
        ReservationRequest reservationRequest = new ReservationRequest(
                bookedReservationSlot.getDate().format(DateTimeFormatter.ISO_DATE),
                bookedReservationSlot.getTime().getId(),
                bookedReservationSlot.getTheme().getId()
        );

//        when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);

        List<MemberReservation> all = memberReservationRepository.findAll();
        Long addedId = all.get(all.size() - 1).getId();
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/waiting/" + addedId)
                .then().log().all()
                .statusCode(204);
    }
}
