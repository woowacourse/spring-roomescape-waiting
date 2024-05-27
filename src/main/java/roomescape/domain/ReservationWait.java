package roomescape.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.proxy.HibernateProxy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.ReservationStatus.Status;
import roomescape.exception.wait.DuplicatedReservationException;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "reservation_wait")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationWait {
    private static final long PRIORITY_INCREMENTAL_STEP = 1L;
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "id", nullable = false)
    private Member member;
    @ManyToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false)
    private Reservation reservation;
    @Embedded
    private ReservationStatus status;

    public ReservationWait(Member member, Reservation reservation, long priority) {
        this.member = member;
        this.reservation = reservation;
        this.status = new ReservationStatus(priority);
    }

    public void validateDuplicateWait(boolean hasSameWait) {
        if (hasSameWait) {
            throw new DuplicatedReservationException();
        }
    }

    public long getPriority() {
        return status.getPriority();
    }

    public long getNextPriority() {
        return status.getPriority() + PRIORITY_INCREMENTAL_STEP;
    }

    public boolean isReserved() {
        return status.isSameAs(Status.RESERVED);
    }

    public void reserve() {
        status.reserve();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        ReservationWait that = (ReservationWait) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();
    }
}
