package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class WeeklyTopTenPopularThemePolicyTest {

    private final PopularThemePolicy policy = new WeeklyTopTenPopularThemePolicy();

    @Test
    void 어제까지_최근_일주일_상위_10개_조건을_생성한다() {
        // given
        LocalDate today = LocalDate.of(2026, 6, 3);

        // when
        PopularThemeCondition result = policy.createCondition(today);

        // then
        assertAll(
                () -> assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 5, 27)),
                () -> assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 2)),
                () -> assertThat(result.getLimit()).isEqualTo(10));
    }
}
