package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record AvailableTimeResponse(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
                                    LocalTime time, Long timeId, boolean isAvailable) {

    public static AvailableTimeResponse of(ReservationTime reservationTime, boolean isReservedTime) {
        return new AvailableTimeResponse(reservationTime.getStartAt(), reservationTime.getId(),isReservedTime);
    }
}
