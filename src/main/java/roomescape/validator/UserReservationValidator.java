package roomescape.validator;

import java.time.LocalDateTime;
import roomescape.domain.Reservation;
import roomescape.exception.custom.CannotCreatePastReservationException;
import roomescape.exception.custom.CannotDeletePastReservationException;

public class UserReservationValidator implements ReservationValidator {

    @Override
    public void validateCreate(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new CannotCreatePastReservationException();
        }
    }

    @Override
    public void validateDelete(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new CannotDeletePastReservationException();
        }
    }
}
