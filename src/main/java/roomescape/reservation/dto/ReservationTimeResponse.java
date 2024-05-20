package roomescape.reservation.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeResponse(Long id, String startAt, Boolean alreadyBooked) {

    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(
                reservationTime.getId(),
                reservationTime.getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                null
        );
    }

    public ReservationTimeResponse(ReservationTime reservationTime, Boolean alreadyBooked) {
        this(
                reservationTime.getId(),
                reservationTime.getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                alreadyBooked
        );
    }

    public ReservationTime toReservationTime() {
        return new ReservationTime(id, LocalTime.parse(startAt));
    }
}
