package roomescape.domain.theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DATE_MAY_EIGHTH;
import static roomescape.TestFixture.DATE_MAY_ONE;

class ThemePopularFilterTest {

    @Test
    @DisplayName("필터링 종료 날짜로 필터링 시작 날짜를 계산한다.")
    void toThemePopularFilter() {
        ThemePopularFilter themePopularFilter = ThemePopularFilter.from(DATE_MAY_EIGHTH);

        assertAll(
                () -> assertThat(themePopularFilter.getStartDate()).isEqualTo(DATE_MAY_ONE),
                () -> assertThat(themePopularFilter.getEndDate()).isEqualTo(DATE_MAY_EIGHTH)
        );
    }
}
