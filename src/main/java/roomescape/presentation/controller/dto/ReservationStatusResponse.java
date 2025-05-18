package roomescape.presentation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.application.dto.ReservationStatusServiceResponse;

public record ReservationStatusResponse(
        long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static ReservationStatusResponse from(ReservationStatusServiceResponse reservationStatus) {
        return new ReservationStatusResponse(
                reservationStatus.reservationId(),
                reservationStatus.theme(),
                reservationStatus.date(),
                reservationStatus.time(),
                reservationStatus.status()
        );
    }
}
