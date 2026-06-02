package roomescape.domain.policy;

import java.time.LocalDate;

public interface PopularThemePolicy {
    LocalDate from(LocalDate today);

    LocalDate to(LocalDate today);

    int limit();

    LocalDate today();
}
