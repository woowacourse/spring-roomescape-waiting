package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.domain.ReservationTime;

public record SelectableTimeResponse(
        long id,

        @JsonFormat(pattern = "kk:mm")
        LocalTime startAt,

        boolean alreadyBooked
) {

    public static SelectableTimeResponse of(final ReservationTime time, final List<Long> usedTimeIds) {
        return new SelectableTimeResponse(
                time.getId(),
                time.getStartAt(),
                isAlreadyBooked(time, usedTimeIds)
        );
    }

    private static boolean isAlreadyBooked(final ReservationTime reservationTime, final List<Long> usedTimeIds) {
        return usedTimeIds.contains(reservationTime.getId());
    }
}
