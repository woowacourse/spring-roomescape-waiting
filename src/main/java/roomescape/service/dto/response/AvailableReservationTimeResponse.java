package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record AvailableReservationTimeResponse(Long id, LocalTime startAt, boolean alreadyBooked) {
    public AvailableReservationTimeResponse(ReservationTime time, boolean alreadyBooked) {
        this(time.getId(), time.getStartAt(), alreadyBooked);
    }
    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }
}
