package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeErrorCode;

public record Reservation(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
    public static Reservation of(String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, name, date, time, theme);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.name, this.date, this.time, this.theme);
    }

    public Reservation update(LocalDate newDate, ReservationTime newTime) {
        LocalDate targetDate = getNewDateValue(newDate);
        ReservationTime targetTime = getNewReservationTimeValue(newTime);

        return new Reservation(
                this.id,
                this.name,
                targetDate,
                targetTime,
                this.theme
        );
    }

    private ReservationTime getNewReservationTimeValue(ReservationTime newTime) {
        if (newTime == null) {
            return this.time;
        }
        return newTime;
    }

    private LocalDate getNewDateValue(LocalDate newDate) {
        if (newDate == null) {
            return this.date;
        }
        return newDate;
    }

    public void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
        }
    }

    public void validateExpiry() {
        LocalDate today = LocalDate.now();
        if (this.date.isBefore(today)) {
            throw new InvalidRequestValueException(ReservationErrorCode.INVALID_DATE.getMessage());
        }
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.of(this.date, this.time.startAt());
        if (current.isAfter(targetTime)) {
            throw new InvalidRequestValueException(TimeErrorCode.INVALID_START_AT.getMessage());
        }
    }

    public boolean hasSameName(String name) {
        return this.name.equals(name);
    }
}
