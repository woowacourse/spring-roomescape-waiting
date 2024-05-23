package roomescape.domain;

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
public class ReservationWait {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWait that = (ReservationWait) o;
        return priority == that.priority && Objects.equals(id, that.id) && Objects.equals(member,
                that.member) && Objects.equals(reservation, that.reservation) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, reservation, priority, status);
    }
}
