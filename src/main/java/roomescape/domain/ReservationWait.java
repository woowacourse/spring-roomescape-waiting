package roomescape.domain;

import static roomescape.domain.ReservationStatus.RESERVED;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import roomescape.exception.wait.DuplicatedReservationException;

@Entity
@Getter
@Table(name = "reservation_wait")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class
ReservationWait {
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
    @Column(name = "priority", nullable = false)
    private long priority;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    public ReservationWait(Member member, Reservation reservation, long priority) {
        this.member = member;
        this.reservation = reservation;
        this.priority = priority;
        this.status = ReservationStatus.valueOf(priority);
    }

    public void validateDuplicateWait(List<ReservationWait> waits) {
        if (waits.isEmpty()) {
            return;
        }
        throw new DuplicatedReservationException();
    }

    public void reserve() {
        this.status = RESERVED;
        this.priority = 0;
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
