package roomescape.waiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.time.dto.TimeResponse;
import roomescape.waiting.ReservationWaiting;

public record ReservationWaitingResponse(
        Long id,
        String name,
        Long themeId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        TimeResponse time,
        Long waitingNumber
) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getThemeId(),
                reservationWaiting.getDate(),
                TimeResponse.from(reservationWaiting.getTime()),
                reservationWaiting.getWaitingNumber()
        );
    }
}
