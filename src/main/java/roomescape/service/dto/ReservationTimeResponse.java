package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public class ReservationTimeResponse {
    private final Long id;
    private final LocalTime startAt;

    public ReservationTimeResponse(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTimeResponse(ReservationTime time) {
        this(time.getId(), time.getStartAt());
    }

    public Long getId() {
        return id;
    }

    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }
}
