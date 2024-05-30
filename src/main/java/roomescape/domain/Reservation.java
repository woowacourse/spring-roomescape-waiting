package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import roomescape.domain.policy.ReservationDueTimePolicy;
import roomescape.exception.reservation.InvalidDateTimeReservationException;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
    private static final long PRIORITY_INCREMENTAL_STEP = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name = "time_id", referencedColumnName = "id", nullable = false)
    private ReservationTime time;
    @ManyToOne
    @JoinColumn(name = "theme_id", referencedColumnName = "id", nullable = false)
    private Theme theme;
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "id", nullable = false)
    private Member member;
    @Embedded
    private ReservationStatus status;


    public Reservation(LocalDate date, ReservationTime time, Theme theme, ReservationStatus status, Member member) {
        this(null, date, time, theme, member, status);
    }

    public void validateDateTimeReservation(ReservationDueTimePolicy timePolicy) {
        LocalDateTime reservationDateTime = date.atTime(time.getStartAt());
        if (reservationDateTime.isBefore(timePolicy.getDueTime())) {
            throw new InvalidDateTimeReservationException();
        }
    }

    public void reserve() {
        status.reserve();
    }

    public long getNextPriority() {
        return status.getPriority() + PRIORITY_INCREMENTAL_STEP;
    }

    public long getPriority() {
        return status.getPriority();
    }

    public boolean isReserved() {
        return status.isSameAs(Status.RESERVED);
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
        Reservation that = (Reservation) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();
    }
}

