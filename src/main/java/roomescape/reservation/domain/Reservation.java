package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.reservation.exception.ReservationException;

import java.time.LocalDateTime;

import static roomescape.reservation.exception.ReservationErrorInformation.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {

    private Long id;
    private String name;
    private Long slotId;
    private ReservationStatus status;
    private LocalDateTime reservedAt;

    public static Reservation reserve(String name, Long slotId, LocalDateTime reservedAt) {
        return of(name, slotId, ReservationStatus.RESERVED, reservedAt);
    }

    public static Reservation wait(String name, Long slotId, LocalDateTime reservedAt) {
        return of(name, slotId, ReservationStatus.WAITING, reservedAt);
    }

    private static Reservation of(String name, Long slotId, ReservationStatus status, LocalDateTime reservedAt) {
        validateName(name);
        return new Reservation(null, name, slotId, status, reservedAt);
    }

    public static Reservation load(Long id, String name, Long slotId, ReservationStatus status, LocalDateTime reservedAt) {
        validateName(name);
        validateId(id);
        return new Reservation(id, name, slotId, status, reservedAt);
    }

    public void cancel(String requesterName) {
        validateOwner(requesterName);
        validateNotCanceled();
        this.status = ReservationStatus.CANCELED;
    }

    public void cancelByManager() {
        this.status = ReservationStatus.CANCELED;
    }

    public void changeSchedule(String requesterName, Long newSlotId) {
        validateOwner(requesterName);
        validateNotCanceled();
        validateNotWaiting();
        this.slotId = newSlotId;
    }

    public void changeScheduleByManager(Long newSlotId) {
        validateNotCanceled();
        validateNotWaiting();
        this.slotId = newSlotId;
    }

    public void validateNotAlreadyCanceled() {
        validateNotCanceled();
    }

    public void validateReschedule(String requesterName) {
        validateOwner(requesterName);
        validateNotCanceled();
        validateNotWaiting();
    }

    public void validateRescheduleByManager() {
        validateNotCanceled();
        validateNotWaiting();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ReservationException(RESERVATION_NAME_IS_NULL);
        }
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new ReservationException(RESERVATION_ID_IS_NULL);
        }
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    private void validateOwner(String requesterName) {
        if (!isOwner(requesterName)) {
            throw new ReservationException(RESERVATION_NOT_OWNER);
        }
    }

    public boolean isOwner(String requesterName) {
        return this.name.equals(requesterName);
    }

    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }

    public boolean isWaiting() {
        return this.status == ReservationStatus.WAITING;
    }

    public Reservation promote() {
        this.status = ReservationStatus.RESERVED;
        return this;
    }

    private void validateNotCanceled() {
        if (status == ReservationStatus.CANCELED) {
            throw new ReservationException(RESERVATION_ALREADY_CANCELED);
        }
    }

    private void validateNotWaiting() {
        if (status == ReservationStatus.WAITING) {
            throw new ReservationException(RESERVATION_ALREADY_WAITING);
        }
    }

}
