package roomescape.service.reservation.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationFilterRequest(
        Long memberId,
        Long themeId,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo
) {
}
