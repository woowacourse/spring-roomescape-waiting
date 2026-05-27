package roomescape.reservationWaiting.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeErrorCode;


public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationWaiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationWaiting of(String name, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationWaiting(null, name, date, time, theme);
    }

    public void validateExpiry(Clock clock) {
        LocalDate nowDate = LocalDate.now(clock);
        if (nowDate.isAfter(date)) {
            throw new InvalidRequestValueException(ReservationErrorCode.INVALID_DATE.getMessage());
        }
        if (nowDate.equals(date) && LocalTime.now(clock).isAfter(time.getStartAt())) {
            throw new InvalidRequestValueException(TimeErrorCode.INVALID_START_AT.getMessage());
        }
    }

    public void validateOwner(String userName) {
        if (!this.name.equals(userName)) {
            throw new ForbiddenException(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaiting that = (ReservationWaiting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
