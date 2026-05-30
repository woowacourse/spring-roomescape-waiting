package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FamousThemeConditionTest {
    private static final LocalDate ANY_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate ANY_NOW = LocalDate.of(2025, 5, 30);

    @Nested
    @DisplayName("days")
    class Days {

        @Test
        void null이면_기본값을_사용한다() {
            FamousThemeCondition condition =
                    new FamousThemeCondition(null, ANY_DATE, 10L, ANY_NOW);

            assertThat(condition.getDays()).isEqualTo(7L);
        }

        @Test
        void 주어진_값이_있으면_그_값을_사용한다() {
            FamousThemeCondition condition =
                    new FamousThemeCondition(30L, ANY_DATE, 10L, ANY_NOW);

            assertThat(condition.getDays()).isEqualTo(30L);
        }
    }

    @Nested
    @DisplayName("date")
    class Date {

        @Test
        void null이면_now를_사용한다() {
            FamousThemeCondition condition =
                    new FamousThemeCondition(7L, null, 10L, ANY_NOW);

            assertThat(condition.getDate()).isEqualTo(ANY_NOW);
        }

        @Test
        void 주어진_값이_있으면_그_값을_사용한다() {
            LocalDate given = LocalDate.of(2025, 3, 15);

            FamousThemeCondition condition =
                    new FamousThemeCondition(7L, given, 10L, ANY_NOW);

            assertThat(condition.getDate()).isEqualTo(given);
        }
    }

    @Nested
    @DisplayName("limit")
    class Limit {

        @Test
        void null이면_기본값을_사용한다() {
            FamousThemeCondition condition =
                    new FamousThemeCondition(7L, ANY_DATE, null, ANY_NOW);

            assertThat(condition.getLimit()).isEqualTo(10L);
        }

        @Test
        void 주어진_값이_있으면_그_값을_사용한다() {
            FamousThemeCondition condition =
                    new FamousThemeCondition(7L, ANY_DATE, 50L, ANY_NOW);

            assertThat(condition.getLimit()).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("날짜 계산")
    class DateCalculate {

        @Test
        void startDate는_date에서_days만큼_뺀_날짜를_반환한다() {
            FamousThemeCondition condition = new FamousThemeCondition(
                    7L, LocalDate.of(2025, 5, 30), 10L, ANY_NOW);

            assertThat(condition.startDate()).isEqualTo(LocalDate.of(2025, 5, 23));
        }

        @Test
        void endDate는_date에서_기본_갭만큼_뺀_날짜를_반환한다() {
            FamousThemeCondition condition = new FamousThemeCondition(
                    7L, LocalDate.of(2025, 5, 30), 10L, ANY_NOW);

            assertThat(condition.endDate()).isEqualTo(LocalDate.of(2025, 5, 29));
        }
    }
}