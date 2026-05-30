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

    private Reservation(Builder builder) {
        this.id = builder.id;
        this.member = builder.member;
        this.slot = builder.slot;
        this.status = builder.status;
        this.deletedAt = builder.deletedAt;
        this.version = builder.version;
    }

    public static Reservation createByUser(Member member, LocalDate date, Time time, Theme theme,
                                           Long storeId, LocalDateTime now) {
        Slot slot = new Slot(date, time, theme, storeId);
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException("지난 시간에 대한 예약 생성은 불가능합니다.");
        }
        return new Builder()
                .member(member).slot(slot)
                .build();
    }

    public static Reservation createByAdmin(Member member, LocalDate date, Time time, Theme theme) {
        return createByAdmin(member, date, time, theme, null);
    }

    public static Reservation createByAdmin(Member member, LocalDate date, Time time, Theme theme, Long storeId) {
        return new Builder()
                .member(member).slot(new Slot(date, time, theme, storeId))
                .build();
    }

    public static Reservation reconstruct(Long id, Member member, LocalDate date, Time time, Theme theme,
                                          ReservationStatus status, LocalDateTime deletedAt, long version,
                                          Long storeId) {
        return new Builder()
                .id(id).member(member).slot(new Slot(date, time, theme, storeId))
                .status(status).deletedAt(deletedAt).version(version)
                .build();
    }

    public static Reservation reconstruct(Long id, Member member, LocalDate date, Time time, Theme theme) {
        return reconstruct(id, member, date, time, theme, ReservationStatus.BOOKED, null, 0L, null);
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

    public void update(LocalDate date, Time time) {
        LocalDateTime now = LocalDateTime.now();
        Slot newSlot = new Slot(date, time, slot.getTheme(), slot.getStoreId());
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
    public Long getStoreId() { return slot.getStoreId(); }
    public ReservationStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    private static class Builder {
        private Long id;
        private Member member;
        private Slot slot;
        private ReservationStatus status = ReservationStatus.BOOKED;
        private LocalDateTime deletedAt;
        private long version = 0L;

        Builder id(Long id) {
            this.id = id;
            return this;
        }

        Builder member(Member member) {
            this.member = member;
            return this;
        }

        Builder slot(Slot slot) {
            this.slot = slot;
            return this;
        }

        Builder status(ReservationStatus status) {
            this.status = status;
            return this;
        }

        Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        Builder version(long version) {
            this.version = version;
            return this;
        }

        Reservation build() {
            return new Reservation(this);
        }
    }
}
