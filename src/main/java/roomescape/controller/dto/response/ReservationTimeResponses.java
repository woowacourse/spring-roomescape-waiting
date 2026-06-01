package roomescape.controller.dto.response;

import roomescape.domain.reservation.ReservationTime;

import java.util.List;

public class ReservationTimeResponses {
    private final List<ReservationTimeResponse> times;

    public ReservationTimeResponses(List<ReservationTimeResponse> times) {
        this.times = times;
    }

    public static ReservationTimeResponses toDto(List<ReservationTime> times) {
        return new ReservationTimeResponses(times.stream()
                .map(ReservationTimeResponse::toDto)
                .toList());
    }

    public List<ReservationTimeResponse> getTimes() {
        return times;
    }
}
