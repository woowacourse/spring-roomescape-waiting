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
        Integer waitingNumber,
        Long amount
) {

    public static Booking fromReservation(Reservation reservation) {
        return new Booking(
                reservation.getId(),
                reservation.getName(),
                reservation.getSession().getDate(),
                reservation.getSession().getTimeSlot(),
                reservation.getSession().getTheme(),
                true,
                null,
                reservation.getAmount()
        );
    }

    public static Booking fromWaiting(Waiting waiting) {
        return new Booking(
                waiting.getId(),
                waiting.getName(),
                waiting.getSession().getDate(),
                waiting.getSession().getTimeSlot(),
                waiting.getSession().getTheme(),
                false,
                waiting.getWaitingNumber(),
                null
        );
    }
}
