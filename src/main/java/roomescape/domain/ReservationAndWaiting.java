package roomescape.domain;

import java.time.LocalDate;

public record ReservationAndWaiting(
        long id,
        String name,
        LocalDate date,
        TimeSlot timeSlot,
        Theme theme,
        boolean isReserved,
        Integer waitingIndex
) {

    public static ReservationAndWaiting fromReservation(Reservation reservation) {
        return new ReservationAndWaiting(reservation.getId(), reservation.getName(), reservation.getDate(), reservation.getTimeSlot(),
                reservation.getTheme(), true, null);
    }

    public static ReservationAndWaiting fromWaiting(WaitingWithNumber waitingWithNumber) {
        Reservation waiting = waitingWithNumber.waiting();
        return new ReservationAndWaiting(waiting.getId(), waiting.getName(), waiting.getDate(), waiting.getTimeSlot(),
                waiting.getTheme(), false, waitingWithNumber.waitingIndex());
    }
}
