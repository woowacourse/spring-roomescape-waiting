package roomescape.domain.reservation;

import java.time.LocalDate;

public interface PopularThemeLookupFilter {

    LocalDate startDate();

    LocalDate endDate();

    int limitCount();
}
