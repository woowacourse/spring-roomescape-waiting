package roomescape.reservation.ui.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.application.dto.ReservationTimeInfo;

public record ReservationTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTimeResponse(final ReservationTimeInfo reservationTimeInfo) {
        this(reservationTimeInfo.id(), reservationTimeInfo.startAt());
    }
}
