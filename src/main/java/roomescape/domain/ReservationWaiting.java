package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;

public class ReservationWaiting {

    private static final String OWNER_CANNOT_WAIT = "본인이 예약한 슬롯에는 대기를 신청할 수 없습니다.";
    private static final String PAST_RESERVATION_WAITING_REJECTED = "지난 시각에는 대기할 수 없습니다.";
    private static final String NOT_OWNER = "본인의 예약 대기가 아닙니다.";

    private final Long id;
    private final Member waiter;
    private final LocalDateTime createdAt;
    private final Slot slot;

    public ReservationWaiting(
            Long id,
            Member waiter,
            LocalDateTime createdAt,
            Slot slot
    ) {
        this.id = id;
        this.waiter = Objects.requireNonNull(waiter);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.slot = Objects.requireNonNull(slot);
    }

    // TODO: 테스트에서 사용하는 메서드
    public ReservationWaiting(
            Long id,
            String name,
            LocalDateTime createdAt,
            Slot slot
    ) {
        this(id, new Member(name), createdAt, slot);
    }

    // TODO: 테스트에서 사용하는 메서드
    public ReservationWaiting(
            String name,
            LocalDateTime createdAt,
            Slot slot
    ) {
        this(null, new Member(name), createdAt, slot);
    }

    // TODO: 테스트에서 사용하는 메서드
    public static ReservationWaiting createWith(
            String name,
            Member reserver,
            Slot slot,
            LocalDateTime now
    ) {
        return createWith(new Member(name), reserver, slot, now);
    }

    // TODO: 이미 있는 예약과 겹치는지는 외부에서 판단하는 것이 좋아보임
    public static ReservationWaiting createWith(
            Member waiter,
            Member reserver,
            Slot slot,
            LocalDateTime now
    ) {
        validateWaitable(waiter, reserver, slot, now);
        return new ReservationWaiting(null, waiter, now, slot);
    }

    private static void validateWaitable(Member waiter, Member reserver, Slot slot, LocalDateTime now) {
        if (waiter.equals(reserver)) {
            throw new BusinessRuleViolationException(OWNER_CANNOT_WAIT);
        }
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_WAITING_REJECTED);
        }
    }

    // TODO: 테스트에서 사용하는 메서드
    public void cancelBy(String name) {
        cancelBy(new Member(name));
    }

    public void cancelBy(Member member) {
        validateOwner(member);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return waiter.name();
    }

    public Member getWaiter() {
        return waiter;
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaiting that = (ReservationWaiting) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void validateOwner(Member member) {
        if (!waiter.equals(member)) {
            throw new ForbiddenException(NOT_OWNER);
        }
    }
}
