package roomescape.domain;

import java.time.LocalDate;

public class Waiting {
    private final Long id;
    private final Long memberId;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;
    private final Long storeId;
    private final Long rank;

    public Long getRank() {
        return rank;
    }

    public Waiting(Long id, Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId, Long rank) {
        this.id = id;
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.storeId = storeId;
        this.rank = rank;
    }

    public Waiting(Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId) {
        this(null, memberId, date, timeId, themeId, storeId, null);
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

    public Long getStoreId() {
        return storeId;
    }
}
