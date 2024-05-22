package roomescape.reservation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTest;
import roomescape.global.exception.ViolationException;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.WaitingReservation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.TOMMY_NAME;
import static roomescape.TestFixture.TOMMY_RESERVATION;
import static roomescape.TestFixture.TOMMY_RESERVATION_DATE;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.USER_TOMMY;
import static roomescape.TestFixture.WOOTECO_THEME;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

class WaitingReservationServiceTest extends ServiceTest {
    @Autowired
    private WaitingReservationService waitingReservationService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberService memberService;

    private ReservationTime miaReservationTime;
    private Theme wootecoTheme;
    private Member mia;
    private Member tommy;

    @BeforeEach
    void setUp() {
        this.miaReservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));
        this.wootecoTheme = themeService.create(WOOTECO_THEME());
        this.mia = memberService.create(USER_MIA());
        this.tommy = memberService.create(USER_TOMMY());
    }

    @Test
    @DisplayName("대기 중인 모든 예약 목록을 조회한다.")
    void findAllInWaitingWithDetails() {
        // given
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        reservationService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));
        reservationService.create(new Reservation(mia, TOMMY_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        // when
        List<Reservation> reservations = waitingReservationService.findWaitingReservations();

        // then
        assertThat(reservations).hasSize(2)
                .extracting(Reservation::getMemberName)
                .contains(TOMMY_NAME, MIA_NAME);
    }

    @Test
    @DisplayName("사용자의 대기 예약 목록을 이전 대기 갯수와 함께 조회한다.")
    void findAllInWaitingWithPreviousCountByMember() {
        // given
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));
        reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when
        List<WaitingReservation> waitingReservations = waitingReservationService.findWaitingReservationsWithPreviousCountByMember(mia);

        // then
        assertThat(waitingReservations).hasSize(1)
                .extracting(WaitingReservation::getPreviousCount)
                .contains(0L);
    }

    @Test
    @DisplayName("사용자 본인의 대기 예약을 취소한다.")
    void deleteMyWaitingReservation() {
        // given
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));

        Reservation waitingReservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when
        waitingReservationService.deleteWaitingReservationByMember(waitingReservation.getId(), mia);

        // then
        List<WaitingReservation> waitingReservations = waitingReservationService.findWaitingReservationsWithPreviousCountByMember(mia);
        assertThat(waitingReservations).hasSize(0);
    }

    @Test
    @DisplayName("다른 사용자의 대기 예약을 취소할 수 없다.")
    void deleteMyWaitingReservationWithoutOwnerShip() {
        // given
        reservationService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));

        Reservation miaWaitingReservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when & then
        assertThatThrownBy(() -> waitingReservationService.deleteWaitingReservationByMember(miaWaitingReservation.getId(), tommy))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("사용자는 확정된 예약을 취소할 수 없다.")
    void deleteMyReservationInBooking() {
        // given
        Reservation reservation = reservationService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        // when & then
        assertThatThrownBy(() -> waitingReservationService.deleteWaitingReservationByMember(reservation.getId(), mia))
                .isInstanceOf(ViolationException.class);
    }
}
