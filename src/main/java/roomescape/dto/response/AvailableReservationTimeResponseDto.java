package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.model.AvailableReservationTime;

public record AvailableReservationTimeResponseDto(
        Long timeId,
        LocalTime startAt,
        boolean alreadyBooked
) {
    public static AvailableReservationTimeResponseDto from(AvailableReservationTime availableReservationTime) {
        return new AvailableReservationTimeResponseDto(
                availableReservationTime.getId(),
                availableReservationTime.getStartAt(),
                availableReservationTime.getAlreadyBooked()
        );
    }

}
