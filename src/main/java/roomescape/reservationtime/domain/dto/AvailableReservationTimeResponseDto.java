package roomescape.reservationtime.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record AvailableReservationTimeResponseDto(
        long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
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
