package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixtures;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;

class ReservationsTest {

    private static final AtomicLong DUMMY_ID_GENERATOR = new AtomicLong();

    private static final User user = TestFixtures.anyUserWithNewId();
    private static final LocalDate date = LocalDate.of(2025, 5, 1);
    private static final TimeSlot time1 = new TimeSlot(LocalTime.of(10, 0));
    private static final TimeSlot time2 = new TimeSlot(LocalTime.of(11, 0));
    private static final TimeSlot time3 = new TimeSlot(LocalTime.of(12, 0));

    @Test
    @DisplayName("가지고 있는 예약들에 대해 예약 횟수를 기준으로 최대 주어진 개수만큼 인기 테마를 찾는다.")
    void findPopularThemes() {
        // given
        var theme1 = TestFixtures.anyThemeWithNewId();
        var theme2 = TestFixtures.anyThemeWithNewId();
        var theme3 = TestFixtures.anyThemeWithNewId();
        var neverReservedTheme = TestFixtures.anyThemeWithNewId();

        var reservations = new Reservations(List.of(
            reservationOf(theme1, date, time1),
            reservationOf(theme1, date, time2),
            reservationOf(theme1, date, time3),

            reservationOf(theme2, date, time1),
            reservationOf(theme2, date, time2),

            reservationOf(theme3, date, time1)
        ));

        // when
        var popularThemes = reservations.findPopularThemes(4);

        // then
        assertAll(
            () -> assertThat(popularThemes).containsSequence(theme1, theme2, theme3),
            () -> assertThat(popularThemes).doesNotContain(neverReservedTheme)
        );
    }

    private Reservation reservationOf(final Theme theme, final LocalDate date, final TimeSlot timeSlot) {
        return new Reservation(
            DUMMY_ID_GENERATOR.incrementAndGet(),
            user,
            ReservationDateTime.of(date, timeSlot),
            theme,
            ReservationStatus.RESERVED
        );
    }
}
