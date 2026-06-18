package roomescape.reservationwaiting.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationWaitingResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime requestAt
) {
    public static ReservationWaitingResponse from(final ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getDate(),
                ThemeResponse.from(reservationWaiting.getTheme()),
                ReservationTimeResponse.from(reservationWaiting.getTime()),
                reservationWaiting.getRequestAt()
        );
    }
}
