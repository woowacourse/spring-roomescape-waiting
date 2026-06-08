package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReservationSlot that)) return false;
        return Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
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
