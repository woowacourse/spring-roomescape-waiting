package roomescape.reservation.domain;

import roomescape.global.exception.InvalidRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record Slot(LocalDate date,
                   ReservationTime time,
                   Theme theme
) {
    public Slot {
        validate(date, time, theme);
    }

    boolean hasSameDateTime(LocalDate date, ReservationTime time) {
        return Objects.equals(this.date, date)
                && Objects.equals(this.time, time);
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt())
                .isBefore(now);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Slot that)) {
            return false;
        }

        return Objects.equals(date, that.date)
                && Objects.equals(time, that.time)
                && isSameTheme(that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, themeKey());
    }

    private boolean isSameTheme(Theme otherTheme) {
        if (theme.getId() == null || otherTheme.getId() == null) {
            return Objects.equals(theme, otherTheme);
        }

        return Objects.equals(theme.getId(), otherTheme.getId());
    }

    private Object themeKey() {
        if (theme.getId() == null) {
            return theme;
        }

        return theme.getId();
    }

    private static void validate(LocalDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new InvalidRequestException("예약 날짜는 비어 있을 수 없습니다.");
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new InvalidRequestException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new InvalidRequestException("테마 정보는 비어 있을 수 없습니다.");
        }
    }
}
