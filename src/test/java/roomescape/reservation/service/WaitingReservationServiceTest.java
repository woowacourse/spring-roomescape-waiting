package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.domain.AuthInfo;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationFixture;
import roomescape.reservation.controller.dto.ReservationWithStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.util.ServiceTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaitingReservationServiceTest extends ServiceTest {

    @Autowired
    ReservationService reservationService;
    @Autowired
    WaitingReservationService waitingReservationService;

    @DisplayName("예약 대기 변경 성공 : 예약 삭제 시 대기 번호가 빠른 예약이 BOOKED 처리된다.")
    @Test
    void waitingReservationConfirm() {
        Reservation bookedMemberReservation = ReservationFixture.getBookedReservation();
        waitingReservationService.deleteMemberReservation(AuthInfo.of(bookedMemberReservation.getMember()), bookedMemberReservation.getId());
        ReservationSlot reservationSlot = bookedMemberReservation.getReservationSlot();

        List<ReservationWithStatus> myReservations = reservationService.findReservations(AuthInfo.of(MemberFixture.getMemberAdmin()));
        ReservationWithStatus nextReservationWithStatus = myReservations.stream()
                .filter(myReservationWithStatus -> myReservationWithStatus.themeName().equals(reservationSlot.getTheme().getName()))
                .filter(myReservationWithStatus -> myReservationWithStatus.time().equals(reservationSlot.getTime().getStartAt()))
                .filter(myReservationWithStatus -> myReservationWithStatus.date().equals(reservationSlot.getDate()))
                .findAny()
                .get();

        assertThat(nextReservationWithStatus.status()).isEqualTo(ReservationStatus.BOOKED);
    }
}
