package roomescape.domain;

import java.time.LocalDate;

public interface PopularThemePolicy {

    PopularThemeCondition createCondition(LocalDate today);
}
