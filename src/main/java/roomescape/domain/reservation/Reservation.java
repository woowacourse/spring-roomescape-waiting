package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.domain.vo.Slot;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.domain.member.Member;

public class Reservation {
    private final Long id;
    private final Member member;
    private final long version;
    private Slot slot;
    private ReservationStatus status;
    private LocalDateTime deletedAt;

    private Reservation(Long id, Member member, Slot slot, ReservationStatus status,
                        LocalDateTime deletedAt, long version) {
        DomainAssert.notNull(member, "예약자는 비어 있을 수 없습니다.");
        DomainAssert.notNull(slot, "슬롯은 비어 있을 수 없습니다.");
        DomainAssert.notNull(status, "예약 상태는 비어 있을 수 없습니다.");
        this.id = id;
        this.member = member;
        this.slot = slot;
        this.status = status;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public static Reservation createByUser(Member member, LocalDate date, Time time, Theme theme,
                                           Store store, LocalDateTime now) {
        Slot slot = new Slot(date, time, theme, store);
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException("지난 시간에 대한 예약 생성은 불가능합니다.");
        }
        return new Reservation(null, member, slot, ReservationStatus.PENDING, null, 0L);
    }

    public static Reservation createByAdmin(Member member, LocalDate date, Time time, Theme theme, Store store) {
        return new Reservation(null, member, new Slot(date, time, theme, store),
                ReservationStatus.BOOKED, null, 0L);
    }

    public static Reservation reconstruct(Long id, Member member, LocalDate date, Time time, Theme theme,
                                          ReservationStatus status, LocalDateTime deletedAt, long version,
                                          Store store) {
        return new Reservation(id, member, new Slot(date, time, theme, store), status, deletedAt, version);
    }

    public void cancelByUser(LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException("지난 예약은 취소 불가능합니다.");
        }
        doCancel(now);
    }

    public void cancelByAdmin(LocalDateTime now) {
        doCancel(now);
    }

    private void doCancel(LocalDateTime now) {
        if (!isActive()) {
            throw new BusinessRuleViolationException("이미 취소된 예약입니다.");
        }
        this.status = ReservationStatus.CANCELED;
        this.deletedAt = now;
    }

    /**
     * 결제 승인 성공 시 결제 대기(PENDING) 예약을 확정(BOOKED)으로 바꾼다.
     */
    public void confirm(LocalDateTime now) {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleViolationException("결제 대기 상태의 예약만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.BOOKED;
    }

    /**
     * 결제 실패/취소 시 결제 대기(PENDING) 예약을 정리한다(취소 처리로 슬롯을 풀어준다).
     */
    public void cancelPending(LocalDateTime now) {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessRuleViolationException("결제 대기 상태의 예약만 정리할 수 있습니다.");
        }
        this.status = ReservationStatus.CANCELED;
        this.deletedAt = now;
    }

    public void update(LocalDate date, Time time, LocalDateTime now) {
        Slot newSlot = new Slot(date, time, slot.getTheme(), slot.getStore());
        if (newSlot.isPast(now)) {
            throw new BusinessRuleViolationException("지난 시간에 대한 예약 수정은 불가능합니다.");
        }
        this.slot = newSlot;
    }

    public boolean isActive() {
        return status == ReservationStatus.BOOKED;
    }

    public boolean isSameMember(Member member) {
        return this.member.equals(member);
    }

    public boolean isInStore(Store store) {
        return slot.isInStore(store);
    }

    public boolean isOnSlot(Slot slot) {
        return this.slot.equals(slot);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public Slot getSlot() { return slot; }
    public LocalDate getDate() { return slot.getDate(); }
    public Time getTime() { return slot.getTime(); }
    public Theme getTheme() { return slot.getTheme(); }
    public Store getStore() { return slot.getStore(); }
    public Long getStoreId() { return slot.getStoreId(); }
    public ReservationStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
