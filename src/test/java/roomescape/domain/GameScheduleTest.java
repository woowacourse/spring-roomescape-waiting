package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;

class GameScheduleTest {

    @DisplayName("날짜, 시간, 테마가 모두 같으면 동일한 게임 일정으로 판단한다.")
    @Test
    void equals() {
        // given
        Theme theme = Theme.withId(1L, "theme", "description", "thumbnail");
        ReservationTime time = ReservationTime.withId(1L, LocalTime.of(9, 0));
        LocalDate date = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.withId(1L, date, time, theme);
        GameSchedule gameSchedule2 = GameSchedule.withId(2L, date, time, theme);

        // when
        boolean isSame = gameSchedule1.equals(gameSchedule2);

        // then
        assertThat(isSame).isTrue();
    }

    @DisplayName("날짜, 시간, 테마 중 하나라도 다르면 동일한 게임 일정이 아니다.")
    @Test
    void notEquals() {
        // given
        Theme theme1 = Theme.withId(1L, "theme1", "description1", "thumbnail1");
        Theme theme2 = Theme.withId(2L, "theme2", "description2", "thumbnail2");
        ReservationTime time1 = ReservationTime.withId(1L, LocalTime.of(9, 0));
        LocalDate date1 = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.withId(1L, date1, time1, theme1);
        GameSchedule gameSchedule2 = GameSchedule.withId(2L, date1, time1, theme2);

        // when
        boolean isSame = gameSchedule1.equals(gameSchedule2);

        // then
        assertThat(isSame).isFalse();
    }
}
