package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.model.ReservationTime;

public record AvailableReservationTimeResponseDto(
        Long timeId,
        LocalTime startAt,
        boolean alreadyBooked
) {
    public static AvailableReservationTimeResponseDto from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new AvailableReservationTimeResponseDto(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked
        );
    }

}
