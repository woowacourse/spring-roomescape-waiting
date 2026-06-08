package roomescape.waiting;

import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.Reservation;
import roomescape.time.ReservationTime;

import java.time.LocalDate;

import static roomescape.global.exception.ErrorCode.FORBIDDEN_RESERVATION_WAITING_ACCESS;

public record WaitingForPromotion(
        Long id,
        String name,
        Long themeId,
        LocalDate date,
        ReservationTime time
) {

    public Reservation toReservation() {
        return new Reservation(name, themeId, date, time);
    }

    public void validateSameName(String name){
        if (!this.name.equals(name)) {
            throw new RoomescapeException(FORBIDDEN_RESERVATION_WAITING_ACCESS);
        }
    }
}
