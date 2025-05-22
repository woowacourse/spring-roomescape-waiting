package roomescape.persistence.vo;

import java.time.LocalDate;

public record Period(
        LocalDate startDate,
        LocalDate endDate
) {
}
