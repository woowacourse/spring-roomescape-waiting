package roomescape.reservation.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationSearchRequest(
        Long themeId,
        Long memberId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
) {

    public boolean isEmpty() {
        return themeId == null && memberId == null && dateFrom == null && dateTo == null;
    }
}
