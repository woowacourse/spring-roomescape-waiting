package roomescape.reservation.domain;

import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_DATE;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.INVALID_RESERVATION_TIME;
import static roomescape.theme.exception.ThemeErrorCode.INVALID_THEME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.DomainException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {

    public ReservationSlot {
        requireNonNull(date, new DomainException(INVALID_RESERVATION_DATE));
        requireNonNull(time, new DomainException(INVALID_RESERVATION_TIME));
        requireNonNull(theme, new DomainException(INVALID_THEME));
    }

    public static ReservationSlot of(LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(date, time, theme);
    }

    public Long timeId() {
        return time.getId();
    }

    public Long themeId() {
        return theme.getId();
    }

    public LocalDateTime startDateTime() {
        return LocalDateTime.of(date, time.getStartAt());
    }

    public boolean isPassed(LocalDateTime now) {
        return startDateTime().isBefore(now);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationSlot that)) {
            return false;
        }
        return Objects.equals(date, that.date)
                && Objects.equals(timeId(), that.timeId())
                && Objects.equals(themeId(), that.themeId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, timeId(), themeId());
    }
}
