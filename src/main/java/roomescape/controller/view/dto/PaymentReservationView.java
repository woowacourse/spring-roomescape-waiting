package roomescape.controller.view.dto;

import java.time.format.DateTimeFormatter;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;

public record PaymentReservationView(
        String reserverName,
        String themeName,
        String date,
        String time
) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy. MM. dd.");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static PaymentReservationView from(Reservation reservation) {
        ReservationSlot slot = reservation.getSlot();
        return new PaymentReservationView(
                reservation.getName(),
                slot.getTheme().getName(),
                slot.getDate().format(DATE_FORMAT),
                slot.getTime().getStartAt().format(TIME_FORMAT)
        );
    }
}
