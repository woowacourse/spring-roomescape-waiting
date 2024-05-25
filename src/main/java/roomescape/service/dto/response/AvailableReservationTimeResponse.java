package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.reservationtime.AvailableReservationTimeDto;

public record AvailableReservationTimeResponse(
        Long timeId,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {

    public static AvailableReservationTimeResponse from(AvailableReservationTimeDto availableReservationTimeDto) {
        return new AvailableReservationTimeResponse(
                availableReservationTimeDto.id(),
                availableReservationTimeDto.startAt(),
                availableReservationTimeDto.alreadyBooked()
        );
    }
}
