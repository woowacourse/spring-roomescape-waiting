package roomescape.theme.domain;

import java.time.LocalDate;

public record PopularThemePeriod(LocalDate from, LocalDate to) {

    public static PopularThemePeriod from(LocalDate today) {
        return new PopularThemePeriod(today.minusWeeks(1), today.minusDays(1));
    }
}