package roomescape.application.policy;

import java.time.LocalDate;

public interface RankingPolicy {
    LocalDate getStartDateAsString();

    LocalDate getEndDateAsString();

    int exposureSize();
}
