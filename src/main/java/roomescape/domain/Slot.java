package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public Long getStoreId() {
        return storeId;
    }
}
