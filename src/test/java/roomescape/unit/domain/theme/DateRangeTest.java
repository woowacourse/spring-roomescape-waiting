package roomescape.unit.domain.theme;

import static org.assertj.core.api.Assertions.*;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import roomescape.domain.theme.DateRange;

class DateRangeTest {

    @Test
    void clock은_null일_수_없다() {
        assertThatThrownBy(() -> DateRange.createLastWeekRange(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 시작일은_현재_기준_7일_전이다() {
        // given
        LocalDate beforeSevenDays = LocalDate.now(FIXED_CLOCK);
        DateRange dateRange = DateRange.createLastWeekRange(FIXED_CLOCK);

        // when
        LocalDate start = dateRange.getStartDate();

        // then
        assertThat(start).isEqualTo(beforeSevenDays.minusDays(7));
    }

    @Test
    void 종료일은_현재_기준_하루_전이다() {
        // given
        LocalDate beforeSevenDays = LocalDate.now(FIXED_CLOCK);
        DateRange dateRange = DateRange.createLastWeekRange(FIXED_CLOCK);

        // when
        LocalDate end = dateRange.getEndDate();

        // then
        assertThat(end).isEqualTo(beforeSevenDays.minusDays(1));
    }
}
