package roomescape.domain.userreservation;

import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.user.User;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationSlotErrors;

@Getter
public class UserReservation {

    private final Long id;
    private final ReservationSlot reservation;
    private final User user;
    private final Long waitingNumber;
    private final WaitingStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private UserReservation(
        Long id,
        ReservationSlot reservation,
        User user,
        Long waitingNumber,
        WaitingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        validate(reservation, user, waitingNumber, status, createdAt, updatedAt);
        this.id = id;
        this.reservation = reservation;
        this.user = user;
        this.waitingNumber = waitingNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserReservation createWithoutId(
        ReservationSlot reservation,
        User user,
        Long waitingNumber,
        WaitingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new UserReservation(null, reservation, user, waitingNumber, status, createdAt, updatedAt);
    }

    public static UserReservation createWithId(long id, UserReservation userReservation) {
        return of(
            id,
            userReservation.getReservation(),
            userReservation.getUser(),
            userReservation.getWaitingNumber(),
            userReservation.getStatus(),
            userReservation.getCreatedAt(),
            userReservation.getUpdatedAt()
        );
    }

    public static UserReservation of(
        long id,
        ReservationSlot reservation,
        User user,
        Long waitingNumber,
        WaitingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new UserReservation(id, reservation, user, waitingNumber, status, createdAt, updatedAt);
    }

    private static void validate(
        ReservationSlot reservation,
        User user,
        Long waitingNumber,
        WaitingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (reservation == null || user == null || status == null || createdAt == null || updatedAt == null) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_USER_RESERVATION);
        }
        if (status == WaitingStatus.WAITING && (waitingNumber == null || waitingNumber < 1)) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_USER_RESERVATION);
        }
    }
}
