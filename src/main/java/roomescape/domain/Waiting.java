package roomescape.domain;

import java.time.LocalDate;

public class Waiting {
    private final Long id;
    private final Long memberId;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public Waiting(Long id, Long memberId, LocalDate date, Long timeId, Long themeId) {
        this.id = id;
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
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
