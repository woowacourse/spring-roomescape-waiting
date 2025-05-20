package roomescape.reservation.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record AdminReservationSearchRequest(
        Long memberId,
        Long themeId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateTo
) {
}
