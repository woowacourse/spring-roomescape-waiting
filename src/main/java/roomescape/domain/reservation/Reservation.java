package roomescape.domain.reservation;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.domain.user.User;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {

    private final Long id;
    private final User user;
    private final ReservationSlot slot;
    private final Integer waitingNumber;
    private final ReservationStatus status;
    private final LocalDateTime reservedAt;

    public static Reservation create(
            User user,
            ReservationSlot slot,
            LocalDateTime reservedAt
    ) {
        return new Reservation(
                null,
                user,
                slot,
                null,
                ReservationStatus.WAITING,
                reservedAt
        );
    }

    public static Reservation of(
            Long id,
            User user,
            ReservationSlot slot,
            Integer waitingNumber,
            ReservationStatus status,
            LocalDateTime reservedAt
    ) {
        return new Reservation(
                id,
                user,
                slot,
                waitingNumber,
                status,
                reservedAt
        );
    }

    public void validateCancellable(LocalDateTime now) {
        slot.validateIsNotInPast(now);
    }

    public Reservation updateConfirmed() {
        return new Reservation(id, user, slot, 0, ReservationStatus.CONFIRMED, reservedAt);
    }

    public Reservation updateWaiting(int waitingNumber) {
        return new Reservation(id, user, slot, waitingNumber, ReservationStatus.WAITING, reservedAt);
    }
}
