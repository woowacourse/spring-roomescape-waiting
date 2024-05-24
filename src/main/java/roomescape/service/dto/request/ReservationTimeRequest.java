package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public class ReservationTimeRequest {
    private final LocalTime startAt;

    @JsonCreator
    public ReservationTimeRequest(LocalTime startAt) {
        validate(startAt);
        this.startAt = startAt;
    }

    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException();
        }
    }

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }

    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }
}
