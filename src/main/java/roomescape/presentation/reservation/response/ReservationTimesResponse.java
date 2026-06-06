package roomescape.presentation.reservation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.ReservationTime;

public record ReservationTimesResponse(
        List<ReservationTimePayload> times
) {

    public static ReservationTimesResponse from(List<ReservationTime> times) {
        List<ReservationTimePayload> payloads = times.stream()
                .map(ReservationTimePayload::from)
                .toList();

        return new ReservationTimesResponse(payloads);
    }

    private record ReservationTimePayload(
            Long id,
            @JsonFormat(pattern = "HH:mm")
            LocalTime startAt
    ) {

        private static ReservationTimePayload from(ReservationTime time) {
            return new ReservationTimePayload(
                    time.getId(),
                    time.getStartAt()
            );
        }
    }
}
