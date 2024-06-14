package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.model.ReservationTime;

public class ReservationTimeResponse {

    private final long id;
    private final LocalTime startAt;

    private ReservationTimeResponse(long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTimeResponse(ReservationTime time) {
        this(time.getId(), time.getStartAt());
    }

    public long getId() {
        return id;
    }

    @JsonFormat(pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }
}
