package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Slot {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Slot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Slot(LocalDate date, ReservationTime time, Theme theme) {
        this(null, date, time, theme);
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isSameSlot(Slot other) {
        return date.equals(other.date)
            && time.getId().equals(other.time.getId())
            && theme.getId().equals(other.theme.getId());
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getId() {
        return id;
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
}
