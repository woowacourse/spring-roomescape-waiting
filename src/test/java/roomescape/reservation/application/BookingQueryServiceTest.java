package roomescape.reservation.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.TOMMY_NAME;
import static roomescape.TestFixture.TOMMY_RESERVATION;
import static roomescape.TestFixture.TOMMY_RESERVATION_TIME;
import static roomescape.TestFixture.WOOTECO_THEME_NAME;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;

class BookingQueryServiceTest extends ReservationServiceTest {
    @Autowired
    private BookingQueryService bookingQueryService;

    @Autowired
    @Qualifier("bookingManageService")
    private ReservationManageService bookingManageService;

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        bookingManageService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));

        // when
        List<Reservation> reservations = bookingQueryService.findAll();

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2)
                    .extracting(Reservation::getMemberName)
                    .contains(MIA_NAME, TOMMY_NAME);
            softly.assertThat(reservations).extracting(Reservation::getTime)
                    .extracting(ReservationTime::getStartAt)
                    .contains(MIA_RESERVATION_TIME, TOMMY_RESERVATION_TIME);
            softly.assertThat(reservations).extracting(Reservation::getTheme)
                    .extracting(Theme::getName)
                    .contains(WOOTECO_THEME_NAME, WOOTECO_THEME_NAME);
        });
    }

    @Test
    @DisplayName("예약자, 테마, 날짜로 예약 목록을 조회한다.")
    void findAllByMemberIdAndThemeIdAndDateBetween() {
        // given
        Reservation miaReservation = bookingManageService.create(
                MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        Reservation tommyReservation = bookingManageService.create(
                TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));

        // when
        List<Reservation> reservations = bookingQueryService.findAllByMemberIdAndThemeIdAndDateBetween(
                miaReservation.getMember().getId(), miaReservation.getTheme().getId(),
                miaReservation.getDate(), tommyReservation.getDate());

        // then
        assertThat(reservations).hasSize(1)
                .extracting(Reservation::getMemberName)
                .containsExactly(MIA_NAME);
    }

    @Test
    @DisplayName("사용자의 예약 목록을 조회한다.")
    void findAllByMember() {
        // given
        bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        bookingManageService.create(TOMMY_RESERVATION(miaReservationTime, wootecoTheme, tommy, BOOKING));

        // when
        List<Reservation> reservations = bookingQueryService.findAllByMember(mia);

        // then
        assertThat(reservations).hasSize(1)
                .extracting(Reservation::getMember)
                .extracting(Member::getId)
                .contains(mia.getId());
    }
}
