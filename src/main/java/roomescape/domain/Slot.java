package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Slot {

    private Long id;
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

    public Slot createWithId(Long id) {
        return new Slot(id, this.date, this.time, this.theme);
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Slot slot = (Slot) object;
        return Objects.equals(id, slot.id) || (Objects.equals(date, slot.date) && Objects.equals(time, slot.time) && Objects.equals(theme, slot.theme));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, time, theme);
    }
}
