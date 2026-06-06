package roomescape.slot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static roomescape.reservation.exception.ReservationErrorInformation.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationSlot {

    private Long id;
    private ReservationDate date;
    private ReservationTime time;
    private Theme theme;
    private Reservations reservations;

    public static ReservationSlot of(ReservationDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        return new ReservationSlot(null, date, time, theme, new Reservations(new ArrayList<>()));
    }

    public static ReservationSlot load(Long id, ReservationDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        return new ReservationSlot(id, date, time, theme, new Reservations(new ArrayList<>()));
    }

    public ReservationSlot withReservations(Reservations reservations) {
        return new ReservationSlot(id, date, time, theme, reservations);
    }

    public Reservation reserve(String requesterName, LocalDateTime reservedAt) {
        validateNotPast(reservedAt);
        return reservations.reserve(requesterName, this.id, reservedAt);
    }

    public Reservations cancel(String requesterName, LocalDateTime requestAt) {
        validateNotPast(requestAt);
        return reservations.cancel(requesterName);
    }

    public Reservations cancelByManager(Long reservationId, LocalDateTime reservedAt) {
        validateNotPast(reservedAt);
        return reservations.cancelByManager(reservationId);
    }

    public Reservations reschedule(ReservationSlot newSlot, String requesterName, LocalDateTime requestAt) {
        this.validateNotPast(requestAt);
        newSlot.validateNotPast(requestAt);
        ReservationStatus status = newSlot.decideStatus(requesterName);
        return reservations.reschedule(newSlot.getId(), requesterName, status);
    }

    public Reservations rescheduleByManager(ReservationSlot newSlot, String requesterName, LocalDateTime requestAt) {
        this.validateNotPast(requestAt);
        newSlot.validateNotPast(requestAt);
        ReservationStatus status = newSlot.decideStatus(requesterName);
        return reservations.rescheduleByManager(newSlot.getId(), requesterName, status);
    }

    private ReservationStatus decideStatus(String requesterName) {
        return reservations.decideStatus(requesterName);
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

    public void validateNotPast(LocalDateTime reservedAt) {
        if (LocalDateTime.of(date.getDate(), time.getStartAt()).isBefore(reservedAt)) {
            throw new ReservationException(RESERVATION_ALREADY_PAST);
        }
    }

    public Long getDateId() {
        return date.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

}
