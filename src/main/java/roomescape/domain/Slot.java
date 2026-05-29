package roomescape.domain;

import roomescape.exception.PastDateTimeException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Slot(
        Schedule schedule,
        Theme theme
) {
    public static Slot from(Schedule schedule, Theme theme) {
        return new Slot(schedule, theme);
    }

    public void validateAvailableTime(LocalDateTime now) {
        if (schedule.isPast(now)) {
            throw new PastDateTimeException("과거의 날짜/시간입니다.");
        }
    }

    public LocalDate date() {
        return schedule.date();
    }

    public ReservationTime time() {
        return schedule.time();
    }
}
