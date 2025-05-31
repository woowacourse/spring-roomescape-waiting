package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationTest {

    @Test
    void 예약_대기_상태를_예약_상태로_변경한다() {
        final Member member = NOT_SAVED_MEMBER_1();
        final LocalDate date = LocalDate.now().plusDays(1);
        final Theme theme = NOT_SAVED_THEME_1();
        final ReservationTime time = NOT_SAVED_RESERVATION_TIME_1();
        final ReservationSlot reservationSlot = new ReservationSlot(date, time, theme, LocalDateTime.now());
        final BookingStatus status = BookingStatus.WAITING;

        // given
        final Reservation reservation = new Reservation(1L, status, member, reservationSlot);

        // when
        reservation.confirmReservation();

        // then
        assertThat(reservation.getStatus()).isEqualTo(BookingStatus.RESERVED);
    }

    @Test
    void 예약_확정_시_이미_예약_상태면_예외가_발생한다() {
        final Member member = NOT_SAVED_MEMBER_1();
        final LocalDate date = LocalDate.now().plusDays(1);
        final Theme theme = NOT_SAVED_THEME_1();
        final ReservationTime time = NOT_SAVED_RESERVATION_TIME_1();
        final ReservationSlot reservationSlot = new ReservationSlot(date, time, theme, LocalDateTime.now());
        final BookingStatus status = BookingStatus.RESERVED;

        // given
        final Reservation reservation = new Reservation(1L, status, member, reservationSlot);

        // when & then
        assertThatThrownBy(reservation::confirmReservation).isInstanceOf(IllegalStateException.class)
                .hasMessage("예약이 대기 중인 경우에만 확정할 수 있습니다.");
    }

}
