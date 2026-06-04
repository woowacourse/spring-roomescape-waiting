package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.user.domain.User;
import roomescape.common.exception.BadRequestException;
import roomescape.common.exception.errors.ReservationSlotErrors;

@Getter
public class Reservation {

    private final Long id;
    private final ReservationSlot reservationSlot;
    private final User user;
    private final Integer waitingNumber;
    private final ReservationStatus status;
    private final LocalDateTime reservedAt;

    private Reservation(
        Long id,
        ReservationSlot reservationSlot,
        User user,
        Integer waitingNumber,
        ReservationStatus status,
        LocalDateTime reservedAt
    ) {
        validate(reservationSlot, user, waitingNumber, status, reservedAt);
        this.id = id;
        this.reservationSlot = reservationSlot;
        this.user = user;
        this.waitingNumber = waitingNumber;
        this.status = status;
        this.reservedAt = reservedAt;
    }

    public static Reservation createWithoutId(
        ReservationSlot reservation,
        User user,
        Integer waitingNumber,
        ReservationStatus status,
        Clock clock
    ) {
        return new Reservation(
            null,
            reservation,
            user,
            waitingNumber,
            status,
            LocalDateTime.now(clock)
        );
    }

    public static Reservation createWithId(long id, Reservation userReservation) {
        return of(
            id,
            userReservation.getReservationSlot(),
            userReservation.getUser(),
            userReservation.getWaitingNumber(),
            userReservation.getStatus(),
            userReservation.getReservedAt()
        );
    }

    public Reservation update(
        ReservationSlot updatedReservationSlot,
        Integer waitingNumber,
        ReservationStatus reservationStatus,
        Clock clock
    ) {
        return new Reservation(
            id,
            updatedReservationSlot,
            user,
            waitingNumber,
            reservationStatus,
            LocalDateTime.now(clock)
        );
    }

    public Reservation update(Clock clock) {
        return new Reservation(
            id,
            reservationSlot,
            user,
            waitingNumber,
            status,
            LocalDateTime.now(clock)
        );
    }

    public static Reservation of(
        long id,
        ReservationSlot reservation,
        User user,
        Integer waitingNumber,
        ReservationStatus status,
        LocalDateTime reservedAt
    ) {
        return new Reservation(id, reservation, user, waitingNumber, status, reservedAt);
    }

    private static void validate(
        ReservationSlot reservation,
        User user,
        Integer waitingNumber,
        ReservationStatus status,
        LocalDateTime reservedAt
    ) {
        if (reservation == null || user == null || status == null || reservedAt == null) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_USER_RESERVATION);
        }
        if (status == ReservationStatus.WAITING && (waitingNumber == null || waitingNumber < 1)) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_USER_RESERVATION);
        }
    }

    public Reservation update(Integer waitingNumber, ReservationStatus status, Clock clock) {
        return new Reservation(
                this.id,
                this.reservationSlot,
                this.user,
                waitingNumber,
                status,
                LocalDateTime.now(clock)
        );
    }
}
