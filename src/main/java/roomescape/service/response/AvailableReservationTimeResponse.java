package roomescape.service.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.time.AvailableReservationTime;

public record AvailableReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean isReserved
) {
    public static AvailableReservationTimeResponse from(final AvailableReservationTime availableReservationTime) {
        return new AvailableReservationTimeResponse(
                availableReservationTime.schedule().getReservationTime().getId(),
                availableReservationTime.schedule().getStartAt(),
                availableReservationTime.available()
        );
    }

    public static List<AvailableReservationTimeResponse> from(
            final List<AvailableReservationTime> availableReservationTimes
    ) {
        return availableReservationTimes.stream().map(AvailableReservationTimeResponse::from).toList();
    }
}
