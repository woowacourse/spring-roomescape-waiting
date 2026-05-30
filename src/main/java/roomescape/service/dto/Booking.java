package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

public record Booking(
        Long id,
        String name,
        LocalDate date,
        TimeSlot timeSlot,
        Theme theme,
        boolean isReserved,
        Integer waitingNumber
) {

    public static Booking fromReservation(Reservation reservation) {
        return new Booking(reservation.getId(), reservation.getName(), reservation.getDate(), reservation.getTimeSlot(),
                reservation.getTheme(), true, null);
    }

    public static Booking fromWaiting(Waiting waiting, TimeSlot timeSlot, Theme theme) {
        return new Booking(waiting.getId(), waiting.getName(), waiting.getDate(), timeSlot, theme, false,
                waiting.getWaitingNumber());
    }
}
