package roomescape.domain.populartheme;

import java.time.LocalDate;

public class WeeklyTopTenPopularThemePolicy implements PopularThemePolicy {

    private static final int TOP_LIMIT = 10;

    @Override
    public PopularThemeCondition createCondition(LocalDate today) {
        LocalDate startDate = today.minusWeeks(1);
        LocalDate endDate = today.minusDays(1);
        return new PopularThemeCondition(startDate, endDate, TOP_LIMIT);
    }
}
