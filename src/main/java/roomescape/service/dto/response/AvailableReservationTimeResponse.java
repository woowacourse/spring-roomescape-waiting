package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public class AvailableReservationTimeResponse {
    private final Long id;
    private final LocalTime startAt;
    private final boolean alreadyBooked;

    public AvailableReservationTimeResponse(Long id, LocalTime startAt, boolean alreadyBooked) {
        this.id = id;
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public AvailableReservationTimeResponse(ReservationTime time, boolean alreadyBooked) {
        this(time.getId(), time.getStartAt(), alreadyBooked);
    }

    public Long getId() {
        return id;
    }

    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isAlreadyBooked() {
        return alreadyBooked;
    }
}
