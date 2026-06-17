package roomescape.common.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;

public class Slot {
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final Store store;

    public Slot(LocalDate date, Time time, Theme theme, Store store) {
        DomainAssert.notNull(date, "예약 날짜는 비어 있을 수 없습니다.");
        DomainAssert.notNull(time, "시간은 비어 있을 수 없습니다.");
        DomainAssert.notNull(theme, "테마는 비어 있을 수 없습니다.");
        DomainAssert.notNull(store, "매장은 비어 있을 수 없습니다.");
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.store = store;
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isInStore(Store store) {
        return this.store.equals(store);
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
        return store.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Slot slot)) {
            return false;
        }
        return Objects.equals(date, slot.date)
                && Objects.equals(time, slot.time)
                && Objects.equals(theme, slot.theme)
                && Objects.equals(store, slot.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme, store);
    }
}
