package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.application.dto.WaitingResult;
import roomescape.reservation.application.dto.WaitingResult.Status;
import roomescape.reservationtime.presentation.dto.ReservationTimeResponse;
import roomescape.theme.presentation.dto.ThemeResponse;

public record WaitingResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        Status status,
        Long rank
) {
    public static WaitingResponse from(WaitingResult result) {
        return new WaitingResponse(
                result.id(),
                result.name(),
                result.date(),
                ThemeResponse.from(result.theme()),
                ReservationTimeResponse.from(result.time()),
                result.status(),
                result.rank()
        );
    }
}
