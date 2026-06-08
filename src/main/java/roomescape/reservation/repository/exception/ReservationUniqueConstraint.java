package roomescape.reservation.repository.exception;

import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Optional;

public enum ReservationUniqueConstraint {
    CONFIRMED_SLOT("uk_reservation_confirmed_slot"),
    WAITING_GUEST_SLOT("uk_reservation_waiting_guest_slot");

    private final String constraintName;

    ReservationUniqueConstraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public static Optional<ReservationUniqueConstraint> from(DuplicateKeyException exception) {
        if (exception == null) {
            return Optional.empty();
        }
        String message = exception.getMessage().toUpperCase();
        return Arrays.stream(values())
                .filter(constraint -> message.contains(constraint.constraintName.toUpperCase()))
                .findFirst();
    }
}
