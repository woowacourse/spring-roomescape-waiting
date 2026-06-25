package roomescape.domain.reservation;

import java.time.LocalDate;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;

public record ReservationAndWaiting(
        long id,
        String name,
        LocalDate date,
        TimeSlot timeSlot,
        Theme theme,
        boolean isReserved,
        Integer waitingIndex,
        ReservationPaymentInfo paymentInfo
) {

    public static ReservationAndWaiting fromReservation(Reservation reservation) {
        return new ReservationAndWaiting(reservation.getId(), reservation.getName(), reservation.getDate(), reservation.getTimeSlot(),
                reservation.getTheme(), true, null, null);
    }

    public static ReservationAndWaiting fromWaiting(WaitingWithNumber waitingWithNumber) {
        Reservation waiting = waitingWithNumber.waiting();
        return new ReservationAndWaiting(waiting.getId(), waiting.getName(), waiting.getDate(), waiting.getTimeSlot(),
                waiting.getTheme(), false, waitingWithNumber.waitingIndex(), null);
    }

    public ReservationAndWaiting withPaymentInfo(ReservationPaymentInfo paymentInfo) {
        return new ReservationAndWaiting(id, name, date, timeSlot, theme, isReserved, waitingIndex, paymentInfo);
    }
}
