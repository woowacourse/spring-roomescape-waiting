package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.user.User;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationSlotErrors;

@Getter
public class Reservation {

    private final Long id;
    private final ReservationSlot reservationSlot;
    private final User user;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Reservation(
        Long id,
        ReservationSlot reservationSlot,
        User user,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        validate(reservationSlot, user, status, createdAt, updatedAt);
        this.id = id;
        this.reservationSlot = reservationSlot;
        this.user = user;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Reservation createWithoutId(
        ReservationSlot reservation,
        User user,
        ReservationStatus status,
        Clock clock
    ) {
        return new Reservation(
            null,
            reservation,
            user,
            status,
            LocalDateTime.now(clock),
            LocalDateTime.now(clock)
        );
    }

    public static Reservation createWithId(long id, Reservation userReservation) {
        return of(
            id,
            userReservation.getReservationSlot(),
            userReservation.getUser(),
            userReservation.getStatus(),
            userReservation.getCreatedAt(),
            userReservation.getUpdatedAt()
        );
    }

    public Reservation update(
        ReservationSlot updatedReservationSlot,
        ReservationStatus reservationStatus,
        Clock clock
    ) {
        return new Reservation(
            id,
            updatedReservationSlot,
            user,
            reservationStatus,
            createdAt,
            LocalDateTime.now(clock)
        );
    }

    public Reservation update(
        ReservationStatus reservationStatus,
        Clock clock
    ) {
        return new Reservation(
            id,
            reservationSlot,
            user,
            reservationStatus,
            createdAt,
            LocalDateTime.now(clock)
        );
    }

    public Reservation update(Clock clock) {
        return new Reservation(
            id,
            reservationSlot,
            user,
            status,
            createdAt,
            LocalDateTime.now(clock)
        );
    }

    public static Reservation of(
        long id,
        ReservationSlot reservation,
        User user,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Reservation(id, reservation, user, status, createdAt, updatedAt);
    }

    private static void validate(
        ReservationSlot reservation,
        User user,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (reservation == null || user == null || status == null || createdAt == null || updatedAt == null) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_USER_RESERVATION);
        }
    }
}
