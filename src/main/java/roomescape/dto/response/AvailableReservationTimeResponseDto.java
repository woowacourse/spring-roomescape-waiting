package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.model.ReservationTime;

public record AvailableReservationTimeResponseDto(
        Long timeId,
        LocalTime startAt,
        boolean alreadyBooked
) {
    public AvailableReservationTimeResponseDto(ReservationTime reservationTime, boolean alreadyBooked) {
        this(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked
        );
    }

}
