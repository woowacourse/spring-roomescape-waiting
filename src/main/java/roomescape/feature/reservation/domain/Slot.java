package roomescape.feature.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;

@Getter
public class Slot {

    private final Schedule schedule;
    private final Theme theme;

    public Slot(LocalDate date, Time time, Theme theme) {
        this.schedule = new Schedule(date, time);
        this.theme = theme;
    }

    public boolean isPast() {
        return schedule.isPast();
    }

    public LocalDate getDate() {
        return schedule.date();
    }

    public Time getTime() {
        return schedule.time();
    }

    public Long getTimeId() {
        return schedule.time().getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public SlotKey toSlotKey() {
        return new SlotKey(getDate(), getTimeId(), getThemeId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Slot other)) {
            return false;
        }
        return Objects.equals(schedule, other.schedule) && Objects.equals(theme, other.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedule, theme);
    }
}
