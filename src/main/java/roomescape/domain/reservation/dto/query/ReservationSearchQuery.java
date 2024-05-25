package roomescape.domain.reservation.dto.query;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import roomescape.domain.reservation.dto.request.ReservationSearchRequest;

public record ReservationSearchQuery(@Nullable Long themeId,
                                     @Nullable Long memberId,
                                     @Nullable LocalDate dateFrom,
                                     @Nullable LocalDate dateTo) {

    public static ReservationSearchQuery from(ReservationSearchRequest request) {
        return new ReservationSearchQuery(
                request.themeId(),
                request.memberId(),
                request.dateFrom(),
                request.dateTo());
    }
}
