package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public class ReservationTimeAvailableResponse {
    private final Long id;
    private final LocalTime startAt;
    private final boolean alreadyBooked;

    @JsonCreator
    public ReservationTimeAvailableResponse(Long id, LocalTime startAt, boolean alreadyBooked) {
        this.id = id;
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public ReservationTimeAvailableResponse(ReservationTime time, boolean alreadyBooked) {
        this(time.getId(), time.getStartAt(), alreadyBooked);
    }

    public Long getId() {
        return id;
    }

    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean getAlreadyBooked() {
        return alreadyBooked;
    }
}
