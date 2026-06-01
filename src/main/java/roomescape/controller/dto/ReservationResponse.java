package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.ReservationResult;

public record ReservationResponse(
        Long id,
        String reserverName,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        long waitingOrder,
        ReservationStatus status
) {
    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.id(),
                result.reserverName(),
                result.date(),
                ReservationTimeResponse.from(result.time()),
                ThemeResponse.from(result.theme()),
                result.waitingOrder(),
                result.status()
        );
    }
}
