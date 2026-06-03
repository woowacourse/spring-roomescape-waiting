package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationSlot {

    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);

        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean hasSameSchedule(ReservationSlot other) {
        return date.equals(other.date) && time.equals(other.time);
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

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date는 비어 있을 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("time은 비어있을 수 없습니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("theme는 비어있을 수 없습니다.");
        }
    }
}
