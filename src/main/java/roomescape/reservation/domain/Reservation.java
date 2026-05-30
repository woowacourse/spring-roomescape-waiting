package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;

import static roomescape.reservation.exception.ReservationErrorInformation.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {

    private Long id;
    private String name;
    private ReservationSlot slot;
    private ReservationStatus status;
    private LocalDateTime reservedAt;

    public static Reservation reserve(String name, ReservationSlot slot, LocalDateTime reservedAt) {
        return of(name, slot, ReservationStatus.RESERVED, reservedAt);
    }

    public static Reservation wait(String name, ReservationSlot slot, LocalDateTime reservedAt) {
        return of(name, slot, ReservationStatus.WAITING, reservedAt);
    }

    private static Reservation of(String name, ReservationSlot slot, ReservationStatus status, LocalDateTime reservedAt) {
        validateName(name);
        validateSlot(slot);
        slot.validateNotPast();
        return new Reservation(null, name, slot, status, reservedAt);
    }

    public static Reservation load(Long id, String name, ReservationSlot slot, ReservationStatus status, LocalDateTime reservedAt) {
        validateName(name);
        validateSlot(slot);
        validateId(id);
        return new Reservation(id, name, slot, status, reservedAt);
    }

    public void cancel(String requesterName) {
        validateOwner(requesterName);
        validateNotCanceled();
        slot.validateNotPast();

        this.status = ReservationStatus.CANCELED;
    }

    public void changeSchedule(String requesterName, ReservationSlot newSlot) {
        validateOwner(requesterName);
        validateNotCanceled();
        validateNotWaiting();
        slot.validateNotPast();
        newSlot.validateNotPast();

        this.slot = newSlot;
    }

    public void changeScheduleByManager(ReservationSlot newSlot) {
        validateNotCanceled();
        validateNotWaiting();
        slot.validateNotPast();
        newSlot.validateNotPast();

        this.slot = newSlot;
    }

    private static void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new ReservationException(RESERVATION_DATE_IS_NULL);
        }
        validateDate(slot.date());
        validateTime(slot.time());
        validateTheme(slot.theme());
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ReservationException(RESERVATION_NAME_IS_NULL);
        }
    }

    private static void validateDate(ReservationDate date) {
        if (date == null) {
            throw new ReservationException(RESERVATION_DATE_IS_NULL);
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new ReservationException(RESERVATION_TIME_IS_NULL);
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new ReservationException(RESERVATION_THEME_IS_NULL);
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

    public ReservationDate getDate() {
        return slot.date();
    }

    public ReservationTime getTime() {
        return slot.time();
    }

    public Theme getTheme() {
        return slot.theme();
    }

}
