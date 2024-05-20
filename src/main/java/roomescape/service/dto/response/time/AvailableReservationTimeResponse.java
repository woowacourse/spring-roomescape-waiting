package roomescape.service.dto.response.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.ReservationTime;

public record AvailableReservationTimeResponse(
        Long id,
        LocalTime startAt,
        boolean alreadyBooked
) {
    public static AvailableReservationTimeResponse of(ReservationTime time, List<ReservationTime> reservedTimes) {
        return new AvailableReservationTimeResponse(
                time.getId(),
                time.getStartAt(),
                time.isAlreadyBooked(reservedTimes)
        );
    }

    @Override
    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime startAt() {
        return startAt;
    }
}
