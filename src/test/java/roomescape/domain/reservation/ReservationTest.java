package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_TIME_SLOT;
import static roomescape.DomainFixtures.JUNK_USER;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DateUtils;
import roomescape.domain.timeslot.TimeSlot;

class ReservationTest {

    @Test
    @DisplayName("예약 일시가 현재 일시보다 이전이면 예외가 발생한다.")
    void isBefore() {
        // given
        var name = JUNK_USER;
        var yesterday = DateUtils.yesterday();
        var timeSlot = JUNK_TIME_SLOT;
        var theme = JUNK_THEME;

        // when & then
        assertThatThrownBy(() -> Reservation.reserveNewly(name, yesterday, timeSlot, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 날짜가 주어진 날짜와 같은 지 확인한다")
    void isDateEquals() {
        // given
        var date = LocalDate.of(2023, 12, 1);
        var reservation = Reservation.ofExisting(1L, JUNK_USER, date, JUNK_TIME_SLOT, JUNK_THEME);

        // when
        boolean dateEquals = reservation.isDateEquals(LocalDate.of(2023, 12, 1));

        // then
        assertThat(dateEquals).isTrue();
    }

    @Test
    @DisplayName("예약 날짜가 주어진 날짜와 같은 지 확인한다")
    void isTimeSlotEquals() {
        // given
        var date = LocalDate.of(2023, 12, 1);
        var timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        var reservation = Reservation.ofExisting(1L, JUNK_USER, date, timeSlot, JUNK_THEME);

        // when
        boolean timeSlotEquals = reservation.isTimeSlotEquals(new TimeSlot(1L, LocalTime.of(10, 0)));

        // then
        assertThat(timeSlotEquals).isTrue();
    }
}
