package roomescape.reservation.domain;

import roomescape.date.domain.ReservationDate;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;
import java.util.List;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;

public record Reservations(
        List<Reservation> values
) {

    public Reservation reserve(
            String requesterName,
            ReservationDate date,
            ReservationTime time,
            Theme theme,
            LocalDateTime reservedAt
    ) {
        validateNotAlreadyBookedBy(requesterName);
        if (hasReservedByOthers(requesterName)) {
            return Reservation.wait(requesterName, date, time, theme, reservedAt);
        }

        return Reservation.create(requesterName, date, time, theme, reservedAt);
    }

    public void validateNotAlreadyBookedBy(String requestName) {
        boolean alreadyBookedByMyself = values.stream()
                .anyMatch(reservation -> reservation.isOwner(requestName));

        if (alreadyBookedByMyself) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

    public boolean hasReservedByOthers(String name) {
        return values.stream()
                .anyMatch(reservation -> !reservation.isOwner(name) && reservation.isReserved());
    }

}
