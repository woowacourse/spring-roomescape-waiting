package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "unique_reservation_waiting_date_time_theme_name",
        columnNames = {"date", "theme_id", "time_id", "name"}
))
public class ReservationWaiting {

    private static final String OWNER_CANNOT_WAIT = "본인이 예약한 슬롯에는 대기를 신청할 수 없습니다.";
    private static final String PAST_RESERVATION_WAITING_REJECTED = "지난 시각에는 대기할 수 없습니다.";
    private static final String NOT_OWNER = "본인의 예약 대기가 아닙니다.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Member waiter;

    @Embedded
    private Slot slot;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ReservationWaiting() {
    }

    public ReservationWaiting(
            Long id,
            Member waiter,
            Slot slot,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.waiter = Objects.requireNonNull(waiter);
        this.slot = Objects.requireNonNull(slot);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static ReservationWaiting createWith(
            Member waiter,
            Member reserver,
            Slot slot,
            LocalDateTime now
    ) {
        validateWaitable(waiter, reserver, slot, now);
        return new ReservationWaiting(
                null,
                waiter,
                slot,
                now
        );
    }

    private static void validateWaitable(Member waiter, Member reserver, Slot slot, LocalDateTime now) {
        if (waiter.equals(reserver)) {
            throw new BusinessRuleViolationException(OWNER_CANNOT_WAIT);
        }
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_WAITING_REJECTED);
        }
    }

    public void cancelBy(Member member) {
        validateOwner(member);
    }

    private void validateOwner(Member member) {
        if (!waiter.equals(member)) {
            throw new ForbiddenException(NOT_OWNER);
        }
    }

    public Long getId() {
        return id;
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
}
