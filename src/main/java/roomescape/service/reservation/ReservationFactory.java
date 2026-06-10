package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;

@Component
public class ReservationFactory {

    public Reservation createNew(
            final String name,
            final ReservationSlot slot,
            final LocalDateTime requestedAt
    ) {
        try {
            return Reservation.createNew(name, slot, requestedAt);
        } catch (IllegalArgumentException exception) {
            throw toInvalidInputException(exception);
        }
    }

    public Reservation changeDateAndTime(
            final Reservation reservation,
            final LocalDate date,
            final ReservationTime reservationTime,
            final LocalDateTime requestedAt
    ) {
        ReservationSlot changedSlot = ReservationSlot.createNew(date, reservation.getTheme(), reservationTime);
        return changeSlot(reservation, changedSlot, requestedAt);
    }

    public Reservation changeSlot(
            final Reservation reservation,
            final ReservationSlot slot,
            final LocalDateTime requestedAt
    ) {
        try {
            return reservation.withSlot(slot, requestedAt);
        } catch (IllegalArgumentException exception) {
            throw toInvalidInputException(exception);
        }
    }

    private InvalidInputException toInvalidInputException(final IllegalArgumentException exception) {
        if (Reservation.PAST_RESERVATION_MESSAGE.equals(exception.getMessage())) {
            return new InvalidInputException(ErrorCode.RESERVATION_DATE_TIME_IN_PAST, exception.getMessage());
        }

        return new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
    }
}
