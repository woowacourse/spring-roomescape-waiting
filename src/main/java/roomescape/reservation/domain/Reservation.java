package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.common.exception.DomainException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonBlank;
import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Getter
public class Reservation {
    private final Long id;
    private final String guestName;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LocalDateTime deletedAt;

    private Reservation(Long id, String guestName, LocalDate date, ReservationTime time, Theme theme, LocalDateTime deletedAt) {
        validateReservation(guestName, date, time, theme);
        this.id = id;
        this.guestName = guestName;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.deletedAt = deletedAt;
    }

    public static Reservation create(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, guestName, date, time, theme, null);
    }

    public static Reservation of(
            long id,
            String guestName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            LocalDateTime deletedAt
    ) {
        return new Reservation(id, guestName, date, time, theme, deletedAt);
    }

    public static Reservation of(long id, String guestName, LocalDate date, ReservationTime time, Theme theme) {
        return of(id, guestName, date, time, theme, null);
    }

    public Reservation withId(long id) {
        require(this.id == null, new DomainException(RESERVATION_ALREADY_HAS_ID));
        return of(id, guestName, date, time, theme, deletedAt);
    }

    private void validateReservation(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        requireNonBlank(guestName, new DomainException(INVALID_RESERVATION_GUEST_NAME));
        requireNonNull(date, new DomainException(INVALID_RESERVATION_DATE));
        requireNonNull(time, new DomainException(INVALID_RESERVATION_TIME));
        requireNonNull(theme, new DomainException(INVALID_THEME));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public boolean isPassed(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt())
                .isBefore(now);
    }

    public boolean isSameGuest(String guestName) {
        return Objects.equals(this.guestName, guestName);
    }

    public Reservation changeDateAndTime(LocalDate changedDate, ReservationTime changedTime) {

        return of(
                id, guestName, changedDate, changedTime, theme, deletedAt
        );
    }
}
