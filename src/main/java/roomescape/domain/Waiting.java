package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.common.exception.BusinessRuleViolationException;

public class Waiting {
    private final Long id;
    private final Member member;
    private final Slot slot;
    private final Long rank;

    private Waiting(Long id, Member member, Slot slot, Long rank) {
        DomainAssert.notNull(member, "대기자는 비어 있을 수 없습니다.");
        DomainAssert.notNull(slot, "슬롯은 비어 있을 수 없습니다.");
        this.id = id;
        this.member = member;
        this.slot = slot;
        this.rank = rank;
    }

    static Waiting create(Member member, Reservation reservation) {
        if (reservation.isSameMember(member)) {
            throw new BusinessRuleViolationException("동일한 사용자의 예약이 존재합니다.");
        }
        return new Waiting(null, member, reservation.getSlot(), null);
    }

    public static Waiting reconstruct(Long id, Member member, LocalDate date, Time time, Theme theme, Store store) {
        return new Waiting(id, member, new Slot(date, time, theme, store), null);
    }

    public Waiting withId(Long id) {
        return new Waiting(id, member, slot, rank);
    }

    Waiting withRank(Long rank) {
        return new Waiting(id, member, slot, rank);
    }

    public boolean isSameMember(Member member) {
        return Objects.equals(this.member.getId(), member.getId());
    }

    public boolean isInStore(Store store) {
        return slot.getStore().equals(store);
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
