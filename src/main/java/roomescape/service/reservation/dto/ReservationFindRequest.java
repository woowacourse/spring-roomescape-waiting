package roomescape.service.reservation.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationFindRequest(
        Long memberId,
        Long themeId,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo
) {
}
