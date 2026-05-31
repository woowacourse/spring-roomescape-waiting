package roomescape.domain;

import java.time.LocalDate;
import roomescape.common.exception.BusinessRuleViolationException;

public class Waiting {
    private final Long id;
    private final Member member;
    private final Slot slot;
    private final Long rank;

    private Waiting(Long id, Member member, Slot slot, Long rank) {
        this.id = id;
        this.member = member;
        this.slot = slot;
        this.rank = rank;
    }

    public static Waiting create(Member member, Reservation reservation) {
        if (reservation.isSameMember(member)) {
            throw new BusinessRuleViolationException("동일한 사용자의 예약이 존재합니다.");
        }
        return new Waiting(null, member, reservation.getSlot(), null);
    }

    public static Waiting reconstruct(Long id, Member member, LocalDate date, Time time, Theme theme, Store store, Long rank) {
        return new Waiting(id, member, new Slot(date, time, theme, store), rank);
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

    public Store getStore() {
        return slot.getStore();
    }

    public Long getStoreId() {
        return slot.getStoreId();
    }

    public Long getRank() {
        return rank;
    }
}
