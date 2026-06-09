package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Slot {
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final Long storeId;

    public Slot(LocalDate date, Time time, Theme theme, Long storeId) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.storeId = storeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Slot other)) {
            return false;
        }
        return Objects.equals(date, other.date)
                && Objects.equals(getTimeId(), other.getTimeId())
                && Objects.equals(getThemeId(), other.getThemeId())
                && Objects.equals(storeId, other.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, getTimeId(), getThemeId(), storeId);
    }
}
