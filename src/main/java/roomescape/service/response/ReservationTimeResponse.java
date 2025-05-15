package roomescape.service.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.time.ReservationTime;

public record ReservationTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static List<ReservationTimeResponse> from(final List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
