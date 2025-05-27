package roomescape.reservationTime.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.reservationTime.domain.ReservationTime;

public record TimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
    public static TimeResponse from(ReservationTime time) {
        return new TimeResponse(time.getId(), time.getStartAt());
    }

    public static List<TimeResponse> from(List<ReservationTime> times) {
        return times.stream()
                .map(TimeResponse::from)
                .toList();
    }
}
