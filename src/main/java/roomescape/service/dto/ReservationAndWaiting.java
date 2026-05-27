package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

public record ReservationAndWaiting(
        String name,
        LocalDate date,
        TimeSlot timeSlot,
        Theme theme,
        boolean isReserved,
        Integer waitingNumber
) {

    public static ReservationAndWaiting fromReservation(Reservation reservation) {
        return new ReservationAndWaiting(reservation.getName(), reservation.getDate(), reservation.getTimeSlot(),
                reservation.getTheme(), true, null);
    }

    public static ReservationAndWaiting fromWaiting(Waiting waiting, TimeSlot timeSlot, Theme theme) {
        return new ReservationAndWaiting(waiting.getName(), waiting.getDate(), timeSlot, theme, false,
                waiting.getWaitingNumber());
    }
}
