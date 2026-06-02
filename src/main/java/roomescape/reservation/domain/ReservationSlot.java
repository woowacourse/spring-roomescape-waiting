package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.common.exception.DomainException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_DATE;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.INVALID_RESERVATION_TIME;
import static roomescape.theme.exception.ThemeErrorCode.INVALID_THEME;

@Getter
public class ReservationSlot {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private ReservationSlot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        validate(date, time, theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationSlot create(LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(null, date, time, theme);
    }

    public static ReservationSlot of(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(id, date, time, theme);
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme) {
        requireNonNull(date, new DomainException(INVALID_RESERVATION_DATE));
        requireNonNull(time, new DomainException(INVALID_RESERVATION_TIME));
        requireNonNull(theme, new DomainException(INVALID_THEME));
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public ReservationSlot changeDateTime(LocalDate changedDate, ReservationTime changedTime) {
        return create(changedDate, changedTime, theme);
    }

    public boolean isPassed(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt())
                .isBefore(now);
    }

    public boolean isSameDateTime(LocalDate date, Long timeId) {
        return this.date.isEqual(date) && Objects.equals(this.time.getId(), timeId);
    }
}
