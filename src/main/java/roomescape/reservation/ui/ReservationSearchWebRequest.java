package roomescape.reservation.ui;

import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

import java.time.LocalDate;

public record ReservationSearchWebRequest(
        Long themeId,
        Long userId,
        LocalDate dateFrom,
        LocalDate dateTo
) {

    public ReservationSearchRequest toServiceRequest() {
        return new ReservationSearchRequest(
                themeId != null ? ThemeId.from(themeId) : null,
                userId != null ? UserId.from(userId) : null,
                dateFrom != null ? ReservationDate.from(dateFrom) : null,
                dateTo != null ? ReservationDate.from(dateTo) : null
        );
    }
}
