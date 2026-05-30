package roomescape.domain;

import java.time.LocalDate;

public class Waiting {
    private final Long id;
    private final Member member;
    private final Slot slot;
    private final Long rank;

    public Waiting(Long id, Member member, Slot slot, Long rank) {
        this.id = id;
        this.member = member;
        this.slot = slot;
        this.rank = rank;
    }

    public Waiting(Long id, Member member, LocalDate date, Time time, Theme theme, Long storeId, Long rank) {
        this(id, member, new Slot(date, time, theme, storeId), rank);
    }

    public Waiting(Member member, LocalDate date, Time time, Theme theme, Long storeId) {
        this(null, member, new Slot(date, time, theme, storeId), null);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public Time getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public Long getStoreId() {
        return slot.getStoreId();
    }

    public Long getRank() {
        return rank;
    }
}
