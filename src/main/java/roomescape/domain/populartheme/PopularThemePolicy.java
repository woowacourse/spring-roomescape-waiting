package roomescape.domain.populartheme;

import java.time.LocalDate;

public interface PopularThemePolicy {

    PopularThemeCondition createCondition(LocalDate today);
}
