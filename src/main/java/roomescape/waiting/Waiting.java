package roomescape.waiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.vo.Slot;
import roomescape.member.Member;
import roomescape.reservation.Reservation;
import roomescape.store.Store;
import roomescape.theme.Theme;
import roomescape.time.Time;

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

    static Waiting create(Member member, Slot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException("지난 시간에 대한 대기 생성은 불가능합니다.");
        }
        return new Waiting(null, member, slot, null);
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
        return this.member.equals(member);
    }

    public Reservation promote(LocalDateTime now) {
        return Reservation.createByUser(member, slot.getDate(), slot.getTime(), slot.getTheme(), slot.getStore(), now);
    }

    public boolean isInStore(Store store) {
        return slot.isInStore(store);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean isOnSlot(Slot slot) {
        return this.slot.equals(slot);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Waiting waiting)) {
            return false;
        }
        return id != null && Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Long getMemberId() {
        return member.getId();
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
