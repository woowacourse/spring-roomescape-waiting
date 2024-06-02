package roomescape.domain.policy;

import java.time.LocalDate;

public class WeeklyRankingPolicy implements RankingPolicy {
    @Override
    public LocalDate getStartDateAsString() {
        return LocalDate.now()
                .minusDays(8);
    }

    @Override
    public LocalDate getEndDateAsString() {
        return LocalDate.now()
                .minusDays(1);
    }

    @Override
    public int exposureSize() {
        return 10;
    }
}
