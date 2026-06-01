package roomescape.domain.reservation;

import roomescape.domain.theme.Theme;
import roomescape.exception.PastDateTimeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Slot {
    private final Schedule schedule;
    private final Theme theme;

    private Slot(Schedule schedule, Theme theme) {
        validateFields(schedule, theme);

        this.schedule = schedule;
        this.theme = theme;
    }

    private void validateFields(Schedule schedule, Theme theme) {
        Objects.requireNonNull(schedule, "일정(schedule)은 비어있을 수 없습니다.");
        Objects.requireNonNull(theme, "테마는 비어있을 수 없습니다.");
    }

    public static Slot from(Schedule schedule, Theme theme) {
        return new Slot(schedule, theme);
    }

    public void validateAvailableTime(LocalDateTime now) {
        if (schedule.isPast(now)) {
            throw new PastDateTimeException("과거의 날짜/시간입니다.");
        }
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public ReservationTime getTime() {
        return schedule.getTime();
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return Objects.equals(schedule, slot.schedule) && Objects.equals(theme, slot.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedule, theme);
    }
}
