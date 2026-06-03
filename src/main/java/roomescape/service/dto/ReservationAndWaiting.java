package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

public record ReservationAndWaiting(
        long id,
        String name,
        LocalDate date,
        TimeSlot timeSlot,
        Theme theme,
        boolean isReserved,
        Integer waitingNumber
) {

    public static ReservationAndWaiting fromReservation(Reservation reservation) {
        return new ReservationAndWaiting(reservation.getId(), reservation.getName(), reservation.getDate(), reservation.getTimeSlot(),
                reservation.getTheme(), true, null);
    }

    public static ReservationAndWaiting fromWaiting(WaitingWithNumber waitingWithNumber) {
        Waiting waiting = waitingWithNumber.waiting();
        return new ReservationAndWaiting(waiting.getId(), waiting.getName(), waiting.getDate(), waiting.getTimeSlot(),
                waiting.getTheme(), false, waitingWithNumber.waitingNumber());
    }
}
