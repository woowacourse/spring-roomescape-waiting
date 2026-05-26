package roomescape.domain;

import java.time.LocalDate;

public class Slot {

    private final Long id;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public Slot(Long id, LocalDate date, Long timeId, Long themeId) {
        this.id = id;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public Slot(LocalDate date, Long timeId, Long themeId) {
        this(null, date, timeId, themeId);
    }

    public Slot createWithId(Long id) {
        return new Slot(id, this.date, this.timeId, this.themeId);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
