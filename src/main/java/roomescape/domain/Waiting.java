package roomescape.domain;

import java.time.LocalDate;

public class Waiting {
    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final Long storeId;
    private final Long rank;

    public Waiting(Long id, Member member, LocalDate date, Time time, Theme theme, Long storeId, Long rank) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.storeId = storeId;
        this.rank = rank;
    }

    public Waiting(Member member, LocalDate date, Time time, Theme theme, Long storeId) {
        this(null, member, date, time, theme, storeId, null);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
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

    public Long getRank() {
        return rank;
    }
}
