package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.domain.AuthInfo;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.MemberReservationFixture;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.MyReservationWithStatus;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.util.ServiceTest;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaitingReservationServiceTest extends ServiceTest {

    @Autowired
    ReservationService reservationService;
    @Autowired
    WaitingReservationService waitingReservationService;

    @DisplayName("예약이 존재하는 경우 대기 순서를 확인할 수 있다.")
    @Test
    void waitingOrder() {
        //given
        AuthInfo authInfo = AuthInfo.of(MemberFixture.getMemberTacan());
        MemberReservation bookedMemberReservation = MemberReservationFixture.getMemberReservation1();
        Reservation bookedReservation = bookedMemberReservation.getReservation();
        reservationService.createMemberReservation(authInfo, new ReservationRequest(bookedReservation.getDate().format(DateTimeFormatter.ISO_DATE), bookedReservation.getTime().getId(), bookedReservation.getTheme().getId()));

        //when
        List<MyReservationWithStatus> myReservations = reservationService.findMyReservations(authInfo);
        List<MyReservationResponse> myReservationResponses = waitingReservationService.handleWaitingOrder(myReservations);

        //then
        MyReservationResponse myReservationResponse = myReservationResponses.get(myReservations.size() - 1);
        assertThat(myReservationResponse.status()).isEqualTo("2번째 대기");
    }
}
