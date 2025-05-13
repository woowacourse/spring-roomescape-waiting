package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixtures.NORMAL_MEMBER_1;
import static roomescape.TestFixtures.RESERVATION_TIME_1;
import static roomescape.TestFixtures.RESERVATION_TIME_2;
import static roomescape.TestFixtures.THEME_1;
import static roomescape.TestFixtures.THEME_2;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.DailyThemeReservations;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;

class DailyThemeReservationsTest {

    @Test
    void 생성시_같은_테마와_같은_날짜가_아닌_예약이_있으면_예외가_발생한다() {
        //given
        List<Reservation> reservations = List.of(
                new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        LocalDate.of(2025, 5, 8),
                        RESERVATION_TIME_1,
                        THEME_1),
                new Reservation(
                        2L,
                        NORMAL_MEMBER_1,
                        LocalDate.of(2025, 5, 8),
                        RESERVATION_TIME_1,
                        THEME_2)
        );

        //when & then
        assertThatThrownBy(() -> new DailyThemeReservations(reservations, 1L, LocalDate.of(2025, 5, 8)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("특정 테마, 특정 날짜에 속한 예약이 아닙니다.");
    }

    @Test
    void 예약된_시간을_계산할_수_있다() {
        //given
        List<Reservation> reservations = List.of(
                new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        LocalDate.of(2025, 5, 8),
                        RESERVATION_TIME_1,
                        THEME_1),
                new Reservation(
                        2L,
                        NORMAL_MEMBER_1,
                        LocalDate.of(2025, 5, 8),
                        RESERVATION_TIME_2,
                        THEME_1)
        );
        DailyThemeReservations dailyThemeReservations = new DailyThemeReservations(reservations, 1L,
                LocalDate.of(2025, 5, 8));

        //when
        Set<ReservationTime> reservationTimes = dailyThemeReservations.calculateBookedTimes();

        //then
        assertThat(reservationTimes).isEqualTo(Set.of(
                RESERVATION_TIME_1,
                RESERVATION_TIME_2
        ));
    }
}