package roomescape.reservation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import roomescape.common.ServiceTest;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.WaitingReservation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

class WaitingBookingQueryServiceTest extends ServiceTest {
    @Autowired
    private WaitingQueryService waitingQueryService;

    @Autowired
    @Qualifier("bookingManageService")
    private ReservationManageService bookingManageService;

    @Autowired
    @Qualifier("waitingManageService")
    private ReservationManageService waitingManageService;

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
        bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        waitingManageService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        bookingManageService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));
        waitingManageService.create(new Reservation(mia, TOMMY_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        // when
        List<Reservation> reservations = waitingQueryService.findWaitingReservations();

        // then
        assertThat(reservations).hasSize(2)
                .extracting(Reservation::getMemberName)
                .contains(TOMMY_NAME, MIA_NAME);
    }

    @Test
    @DisplayName("사용자의 대기 예약 목록을 이전 대기 갯수와 함께 조회한다.")
    void findAllInWaitingWithPreviousCountByMember() {
        // given
        bookingManageService.create(new Reservation(tommy, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING));
        waitingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, WAITING));

        // when
        List<WaitingReservation> waitingReservations = waitingQueryService.findWaitingReservationsWithPreviousCountByMember(mia);

        // then
        assertThat(waitingReservations).hasSize(1)
                .extracting(WaitingReservation::getPreviousCount)
                .contains(0L);
    }
}
