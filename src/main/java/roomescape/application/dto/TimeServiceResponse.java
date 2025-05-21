package roomescape.application.dto;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.ReservationTime;

public record TimeServiceResponse(
        long id,
        LocalTime startAt
) {

    public static TimeServiceResponse from(ReservationTime reservationTime) {
        return new TimeServiceResponse(reservationTime.getId(), reservationTime.getStartAt());
    }

    public static List<TimeServiceResponse> from(List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(TimeServiceResponse::from)
                .toList();
    }

    public ReservationTime toEntity() {
        return ReservationTime.of(id, startAt);
    }
}
