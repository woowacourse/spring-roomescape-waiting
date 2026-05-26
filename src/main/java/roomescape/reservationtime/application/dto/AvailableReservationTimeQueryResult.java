package roomescape.reservationtime.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;

public record AvailableReservationTimeQueryResult(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean available
) {

    public static AvailableReservationTimeQueryResult from(AvailableReservationTime time) {
        return new AvailableReservationTimeQueryResult(
                time.id(),
                time.startAt(),
                time.available()
        );
    }
}
