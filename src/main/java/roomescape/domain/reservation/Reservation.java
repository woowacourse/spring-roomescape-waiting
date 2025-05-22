package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.domain.user.User;
import roomescape.exception.BusinessRuleViolationException;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    private User user;
    @Embedded
    private ReservationSlot slot;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation(final long id, final User user, final ReservationSlot slot, final ReservationStatus status) {
        this.id = id;
        this.user = user;
        this.slot = slot;
        this.status = status;
    }

    public Reservation(final User user, final ReservationSlot slot) {
        this(0L, user, slot, ReservationStatus.RESERVED);
    }

    public static Reservation ofWaiting(final User user, final ReservationSlot slot) {
        return new Reservation(0L, user, slot, ReservationStatus.WAITING);
    }

    public boolean isOwnedBy(final User user) {
        return this.user.equals(user);
    }

    public void cancel() {
        if (this.status != ReservationStatus.WAITING) {
            throw new BusinessRuleViolationException("대기중인 예약만 취소할 수 있습니다.");
        }
        this.status = ReservationStatus.CANCELED;
    }

    @Override
    public String toString() {
        return "Reservation{" +
               "id=" + id +
               ", userId=" + user.id() +
               ", slot=" + slot +
               ", status=" + status +
               '}';
    }
}

