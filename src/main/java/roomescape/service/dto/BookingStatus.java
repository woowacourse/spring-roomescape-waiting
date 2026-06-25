package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.domain.WaitingWithTurn;

public record BookingStatus(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        BookingType bookingType,
        ReservationStatus reservationStatus,
        Long turn
) {
    public static BookingStatus reservation(Reservation reservation) {
        ReservationSlot slot = reservation.getSlot();
        return new BookingStatus(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                slot.getTime(),
                slot.getTheme(),
                BookingType.RESERVATION,
                reservation.getStatus(),
                null
        );
    }

    public static BookingStatus waiting(WaitingWithTurn waitingWithTurn) {
        ReservationWaiting waiting = waitingWithTurn.waiting();
        ReservationSlot slot = waiting.getSlot();
        return new BookingStatus(
                waiting.getId(),
                waiting.getName(),
                slot.getDate(),
                slot.getTime(),
                slot.getTheme(),
                BookingType.WAITING,
                null,
                waitingWithTurn.turn()
        );
    }
}
