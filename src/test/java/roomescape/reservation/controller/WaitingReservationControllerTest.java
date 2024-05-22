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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.util.ControllerTest;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static roomescape.fixture.MemberFixture.getMemberTacan;

class WaitingReservationControllerTest extends ControllerTest {

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
        MemberReservation bookedMemberReservation = MemberReservationFixture.getMemberReservation1();
        Reservation alreadBookedReservation = bookedMemberReservation.getReservation();
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

    @DisplayName("예약 대기인 예약을 삭제한다.")
    @Test
    void delete() {
        //given
        MemberReservation bookedMemberReservation = MemberReservationFixture.getMemberReservation1();
        Reservation bookedReservation = bookedMemberReservation.getReservation();
        ReservationRequest reservationRequest = new ReservationRequest(
                bookedReservation.getDate().format(DateTimeFormatter.ISO_DATE),
                bookedReservation.getTime().getId(),
                bookedReservation.getTheme().getId()
        );

        //when & then
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
