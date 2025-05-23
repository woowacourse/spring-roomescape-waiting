package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.fixture.ReservationDateFixture;

class ReservationScheduleTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(
            1L,
            new ThemeName("공포"),
            new ThemeDescription("무섭다"),
            new ThemeThumbnail("thumb.jpg")
    );

    @Test
    void 날짜는_null일_수_없다() {
        assertThatThrownBy(() -> new ReservationSchedule(1L, null, time, theme))
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void 시간은_null일_수_없다() {
        assertThatThrownBy(() -> new ReservationSchedule(1L, ReservationDateFixture.예약날짜_오늘, null, theme))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 테마는_null일_수_없다() {
        assertThatThrownBy(() -> new ReservationSchedule(1L, ReservationDateFixture.예약날짜_오늘, time, null))
                .isInstanceOf(NullPointerException.class);
    }

}
