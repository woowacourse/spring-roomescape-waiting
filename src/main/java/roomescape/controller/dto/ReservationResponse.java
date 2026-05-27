package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.service.dto.ReservationResult;

public record ReservationResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long waitingOrder
) {
    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.time()),
                ThemeResponse.from(result.theme()),
                result.waitingOrder()
        );
    }
}
