package roomescape.reservation.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import roomescape.global.exception.ViolationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingReservation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

class WaitingManageServiceTest extends ReservationServiceTest {
    @Autowired
    @Qualifier("waitingManageService")
    private ReservationManageService waitingManageService;

    @Autowired
    @Qualifier("bookingManageService")
    private ReservationManageService bookingManageService;

    @Autowired
    private WaitingQueryService waitingQueryService;

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 예약이 있다면 대기 예약을 한다.")
    void create() {
        // given
        bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        Reservation waitingReservation = new Reservation(
                tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING);
        Reservation createdReservation = waitingManageService.create(waitingReservation);

        // when
        Reservation scheduledReservation = waitingManageService.scheduleRecentReservation(createdReservation);

        // then
        assertSoftly(softly -> {
            softly.assertThat(scheduledReservation.getId()).isNotNull();
            softly.assertThat(scheduledReservation.getStatus()).isEqualTo(WAITING);
        });
    }

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 예약이 없다면 대기할 수 없다.")
    void createInvalidWaitingReservation() {
        // given
        Reservation waitingReservation = new Reservation(
                tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING);
        Reservation createdReservation = waitingManageService.create(waitingReservation);

        // when
        Reservation scheduledReservation = waitingManageService.scheduleRecentReservation(createdReservation);

        // then
        assertThat(scheduledReservation.getStatus()).isEqualTo(BOOKING);
    }

    @Test
    @DisplayName("사용자 본인의 대기 예약을 취소한다.")
    void delete() {
        // given
        bookingManageService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));

        Reservation waitingReservation = waitingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when
        waitingManageService.delete(waitingReservation.getId(), mia);

        // then
        List<WaitingReservation> waitingReservations = waitingQueryService.findAllWithPreviousCountByMember(mia);
        assertThat(waitingReservations).hasSize(0);
    }

    @Test
    @DisplayName("관리자가 대기 예약을 취소한다.")
    void deleteWithAdmin() {
        // given
        bookingManageService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));
        Reservation waitingReservation = waitingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when
        waitingManageService.delete(waitingReservation.getId(), admin);

        // then
        List<WaitingReservation> waitingReservations = waitingQueryService.findAllWithPreviousCountByMember(mia);
        assertThat(waitingReservations).hasSize(0);
    }

    @Test
    @DisplayName("다른 사용자의 대기 예약을 취소할 수 없다.")
    void deleteWithoutOwnerShip() {
        // given
        bookingManageService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));

        Reservation miaWaitingReservation = waitingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when & then
        assertThatThrownBy(() -> waitingManageService.delete(miaWaitingReservation.getId(), tommy))
                .isInstanceOf(ViolationException.class);
    }
}
