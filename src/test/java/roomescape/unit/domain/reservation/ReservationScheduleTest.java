package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;

class ReservationScheduleTest {

    private final ReservationDate date = new ReservationDate(LocalDate.of(2025, 5, 24));
    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(
            1L,
            new ThemeName("공포"),
            new ThemeDescription("무섭다"),
            new ThemeThumbnail("thumb.jpg")
    );

    @Test
    void reservationDate는_null일_수_없다() {
        assertThatThrownBy(() ->
                new ReservationSchedule(null, time, theme)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void reservationTime은_null일_수_없다() {
        assertThatThrownBy(() ->
                new ReservationSchedule(date, null, theme)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void theme은_null일_수_없다() {
        assertThatThrownBy(() ->
                new ReservationSchedule(date, time, null)
        ).isInstanceOf(NullPointerException.class);
    }
}
