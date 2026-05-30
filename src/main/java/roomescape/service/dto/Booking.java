package roomescape.service.dto;

import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

import java.time.LocalDate;

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
        return new Booking(reservation.getId(), reservation.getName(), reservation.getDate(),
                reservation.getTimeSlot(), reservation.getTheme(), true, null);
    }

    public static Booking fromWaiting(Waiting waiting) {
        return new Booking(waiting.getId(), waiting.getName(), waiting.getDate(),
                waiting.getTimeSlot(), waiting.getTheme(), false, waiting.getWaitingNumber());
    }
}
