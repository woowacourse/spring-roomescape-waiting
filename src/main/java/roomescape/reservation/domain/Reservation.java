package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static roomescape.reservation.exception.ReservationErrorInformation.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {

    private Long id;
    private String name;
    private ReservationDate date;
    private ReservationTime time;
    private Theme theme;
    private ReservationStatus status;
    private LocalDateTime reservedAt;

    public static Reservation reserve(String name, ReservationDate reservationDate, ReservationTime time, Theme theme, LocalDateTime reservedAt) {
        return of(name, reservationDate, time, theme, ReservationStatus.RESERVED, reservedAt);
    }

    public static Reservation wait(String name, ReservationDate reservationDate, ReservationTime time, Theme theme, LocalDateTime reservedAt) {
        return of(name, reservationDate, time, theme, ReservationStatus.WAITING, reservedAt);
    }

    private static Reservation of(String name, ReservationDate reservationDate, ReservationTime time, Theme theme, ReservationStatus status, LocalDateTime reservedAt) {
        validate(name, reservationDate, time, theme);
        validateReservedAtIsNotPast(reservedAt);
        validatePast(reservationDate.getDate(), time.getStartAt());
        return new Reservation(null, name, reservationDate, time, theme, status, reservedAt);
    }

    public static Reservation load(Long id, String name, ReservationDate reservationDate, ReservationTime time, Theme theme, ReservationStatus status, LocalDateTime reservedAt) {
        validate(name, reservationDate, time, theme);
        validateId(id);
        return new Reservation(id, name, reservationDate, time, theme, status, reservedAt);
    }

    public void cancel(String requesterName) {
        validateOwner(requesterName);
        validateNotCanceled();
        validateNotPast(date.getDate(), time.getStartAt());

        this.status = ReservationStatus.CANCELED;
    }

    public void changeSchedule(String requesterName, ReservationDate newDate, ReservationTime newTime) {
        validateOwner(requesterName);
        validateNotCanceled();
        validateNotWaiting();
        validateNotPast(date.getDate(), time.getStartAt());
        validateNewScheduleIsPast(newDate.getDate(), newTime.getStartAt());

        this.date = newDate;
        this.time = newTime;
    }

    public void changeScheduleByManager(ReservationDate newDate, ReservationTime newTime) {
        validateNotCanceled();
        validateNotWaiting();
        validateNotPast(date.getDate(), time.getStartAt());
        validateNewScheduleIsPast(newDate.getDate(), newTime.getStartAt());

        this.date = newDate;
        this.time = newTime;
    }

    private static void validate(String name, ReservationDate reservationDate, ReservationTime time, Theme theme) {
        validateName(name);
        validateDate(reservationDate);
        validateTime(time);
        validateTheme(theme);
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

    private static void validatePast(LocalDate date, LocalTime time) {
        if (isPast(date, time)) {
            throw new ReservationException(RESERVATION_PAST_DATETIME_NOT_ALLOWED);
        }
    }

    private static void validateReservedAtIsNotPast(LocalDateTime reservedAt) {
        if (reservedAt.isBefore(LocalDateTime.now())) {
            throw new ReservationException(RESERVATION_RESERVED_AT_PAST_NOT_ALLOWED);
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

    private void validateNotPast(LocalDate date, LocalTime time) {
        if (isPast(date, time)) {
            throw new ReservationException(RESERVATION_ALREADY_PAST);
        }
    }

    private void validateNewScheduleIsPast(LocalDate date, LocalTime time) {
        if (isPast(date, time)) {
            throw new ReservationException(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED);
        }
    }

    private static boolean isPast(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time).isBefore(LocalDateTime.now());
    }

}
