package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.domain.AuthInfo;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.MemberReservationFixture;
import roomescape.reservation.controller.dto.MyReservationWithStatus;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.util.ServiceTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaitingReservationSlotServiceTest extends ServiceTest {

    @Autowired
    ReservationService reservationService;
    @Autowired
    WaitingReservationService waitingReservationService;

    @DisplayName("예약 대기 변겅 성공 : 예약 삭제 시 대기 번호가 빠른 예약이 BOOKED 처리된다.")
    @Test
    void waitingReservationConfirm() {
        MemberReservation bookedMemberReservation = MemberReservationFixture.getBookedMemberReservation();
        waitingReservationService.deleteMemberReservation(AuthInfo.of(bookedMemberReservation.getMember()), bookedMemberReservation.getId());
        ReservationSlot reservationSlot = bookedMemberReservation.getReservationSlot();

        List<MyReservationWithStatus> myReservations = reservationService.findMyReservations(AuthInfo.of(MemberFixture.getMemberAdmin()));
        MyReservationWithStatus nextReservationWithStatus = myReservations.stream()
                .filter(myReservationWithStatus -> myReservationWithStatus.themeName().equals(reservationSlot.getTheme().getName()))
                .filter(myReservationWithStatus -> myReservationWithStatus.time().equals(reservationSlot.getTime().getStartAt()))
                .filter(myReservationWithStatus -> myReservationWithStatus.date().equals(reservationSlot.getDate()))
                .findAny()
                .get();

        assertThat(nextReservationWithStatus.status()).isEqualTo(ReservationStatus.BOOKED);
    }
}
