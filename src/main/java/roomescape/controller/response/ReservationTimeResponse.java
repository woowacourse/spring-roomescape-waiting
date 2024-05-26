package roomescape.controller.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.model.ReservationTime;

public class ReservationTimeResponse {

    private Long id;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startAt;

    public static ReservationTimeResponse of(ReservationTime domain) {
        return new ReservationTimeResponse(domain.getId(), domain.getStartAt());
    }

    public ReservationTimeResponse() {
    }

    public ReservationTimeResponse(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
