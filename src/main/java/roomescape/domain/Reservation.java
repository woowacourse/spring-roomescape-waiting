package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.BusinessRuleViolationException;

public class Reservation {
    private final Long id;
    private final Member member;
    private final long version;
    private Slot slot;
    private ReservationStatus status;
    private LocalDateTime deletedAt;

    private Reservation(Long id, Member member, Slot slot, ReservationStatus status,
                        LocalDateTime deletedAt, long version) {
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
        return new Reservation(null, member, slot, ReservationStatus.BOOKED, null, 0L);
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
        return Objects.equals(this.member.getId(), member.getId());
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
