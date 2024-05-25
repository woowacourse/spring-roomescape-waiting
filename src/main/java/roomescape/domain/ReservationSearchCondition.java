package roomescape.domain;

import java.time.LocalDate;

public record ReservationSearchCondition(
    long memberId,
    long themeId,
    LocalDate start,
    LocalDate end
) {
}
