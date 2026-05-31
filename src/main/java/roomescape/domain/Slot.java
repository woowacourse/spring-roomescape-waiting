package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Slot {
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final Store store;

    public Slot(LocalDate date, Time time, Theme theme, Store store) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.store = store;
    }

    public boolean isPast(LocalDateTime now) {
        return time.isReservationBefore(now, date);
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

    public Store getStore() {
        return store;
    }

    public Long getStoreId() {
        return store == null ? null : store.getId();
    }
}
