package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationSearch;

public record ReservationSearchRequest(Long themeId, Long memberId, LocalDate startDate, LocalDate endDate) {
    public ReservationSearch createReservationSearch() {
        return new ReservationSearch(themeId, memberId, startDate, endDate);
    }
}
