package roomescape.domain.policy;

import static java.time.Month.FEBRUARY;

import java.time.LocalDate;

public class FixedDateWeeklyRankingPolicy implements RankingPolicy {

    @Override
    public LocalDate getStartDateAsString() {
        return LocalDate.of(2023, FEBRUARY, 8)
                .minusDays(8);
    }

    @Override
    public LocalDate getEndDateAsString() {
        return LocalDate.of(2023, FEBRUARY, 8)
                .minusDays(1);
    }

    @Override
    public int exposureSize() {
        return 10;
    }
}
