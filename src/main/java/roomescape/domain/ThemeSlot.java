package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;

public class ThemeSlot {

    private final Long id;
    private final Theme theme;
    private final LocalDate date;
    private final Time time;
    private boolean isReserved;

    public ThemeSlot(Theme theme, LocalDate date, Time time, boolean isReserved) {
        this.id = null;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.isReserved = isReserved;
    }

    public ThemeSlot(Long id, Theme theme, LocalDate date, Time time, boolean isReserved) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.isReserved = isReserved;
    }

    public static ThemeSlot of(Long id, ThemeSlot themeSlot) {
        return new ThemeSlot(id, themeSlot.getTheme(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.isReserved());
    }

    public Long getId() {
        return id;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public boolean hasSameId(Long id) {
        return Objects.equals(this.id, id);
    }

    public void reserve() {
        this.isReserved = true;
    }
}
