package roomescape.dto.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record AvailableReservationTimeResponseDto(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {

    public AvailableReservationTimeResponseDto(
            ReservationTime reservationTime,
            boolean alreadyBooked
    ) {
        this(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked
        );
    }
}
