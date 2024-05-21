package roomescape.web.dto.response.time;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import roomescape.domain.ReservationTime;

public record ReservationTimeResponse(Long id, LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTime time) {
        return new ReservationTimeResponse(
                time.getId(),
                time.getStartAt()
        );
    }

    @Override
    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime startAt() {
        return startAt;
    }
}
