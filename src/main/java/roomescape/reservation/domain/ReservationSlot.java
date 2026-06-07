package roomescape.reservation.domain;

import roomescape.global.exception.InvalidRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record ReservationSlot(
        Theme theme,
        LocalDate date,
        ReservationTime time
) {

    public static ReservationSlot of(Theme theme, LocalDate date, ReservationTime time) {
        validateTheme(theme);
        validateDate(date);
        validateTime(time);
        return new ReservationSlot(theme, date, time);
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isSameSlot(ReservationSlot other) {
        if (other == null) {
            return false;
        }

        return Objects.equals(date, other.date)
                && Objects.equals(time.getId(), other.time.getId())
                && Objects.equals(theme.getId(), other.theme.getId());
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new InvalidRequestException("테마 정보는 비어 있을 수 없습니다.");
        }
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
}
