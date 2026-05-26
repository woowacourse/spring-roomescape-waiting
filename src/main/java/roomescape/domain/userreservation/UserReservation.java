package roomescape.domain.userreservation;

import lombok.Getter;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationErrors;

@Getter
public class UserReservation {

    private final Long id;
    private final Long reservationId;
    private final Long userId;
    private final Long waitingNumber;
    private final WaitingStatus status;

    private UserReservation(
        Long id,
        Long reservationId,
        Long userId,
        Long waitingNumber,
        WaitingStatus status
    ) {
        validate(reservationId, userId, waitingNumber, status);
        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.waitingNumber = waitingNumber;
        this.status = status;
    }

    public static UserReservation createWithoutId(
        Long reservationId,
        Long userId,
        Long waitingNumber,
        WaitingStatus status
    ) {
        return new UserReservation(null, reservationId, userId, waitingNumber, status);
    }

    public static UserReservation createWithId(long id, UserReservation userReservation) {
        return of(
            id,
            userReservation.getReservationId(),
            userReservation.getUserId(),
            userReservation.getWaitingNumber(),
            userReservation.getStatus()
        );
    }

    public static UserReservation of(
        long id,
        Long reservationId,
        Long userId,
        Long waitingNumber,
        WaitingStatus status
    ) {
        return new UserReservation(id, reservationId, userId, waitingNumber, status);
    }

    private static void validate(Long reservationId, Long userId, Long waitingNumber, WaitingStatus status) {
        if (reservationId == null || userId == null || status == null) {
            throw new BadRequestException(ReservationErrors.INVALID_USER_RESERVATION);
        }
        if (status == WaitingStatus.WAITING && (waitingNumber == null || waitingNumber < 1)) {
            throw new BadRequestException(ReservationErrors.INVALID_USER_RESERVATION);
        }
    }
}
