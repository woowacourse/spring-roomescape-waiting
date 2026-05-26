package roomescape.domain.waiting;

import java.time.LocalDate;

public class Waiting {

    private final Long themeId;
    private final Long timeId;
    private final LocalDate date;
    private final String name;

    public Waiting(Long themeId, Long timeId, LocalDate date, String name) {
        this.themeId = themeId;
        this.timeId = timeId;
        this.date = date;
        this.name = name;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getTimeId() {
        return timeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }
}
