package roomescape.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.application.dto.result.ReservationTimeResult;

public class ReservationTimeResponse {
    private final Long id;

    @JsonFormat(pattern = "HH:mm")
    private final LocalTime startAt;

    public ReservationTimeResponse(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTimeResponse from(ReservationTimeResult result) {
        return new ReservationTimeResponse(result.getId(), result.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
