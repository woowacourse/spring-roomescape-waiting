package roomescape.domain;

import static roomescape.domain.ReservationStatus.RESERVED;

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

@Entity
@Getter
@Table(name = "reservation_wait")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationWait {
    @Column(insertable = false)
    private static final int RESERVED_PRIORITY_NUMBER = 0;

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
    private int priority;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    public ReservationWait(Member member, Reservation reservation, int priority, ReservationStatus status) {
        validatePriority(priority, status);
        this.member = member;
        this.reservation = reservation;
        this.priority = priority;
        this.status = status;
    }

    private void validatePriority(int priority, ReservationStatus status) {
        if (priority != RESERVED_PRIORITY_NUMBER && status == RESERVED) {
            throw new IllegalArgumentException("잘못된 생성자 인자입니다");
        }
        if (priority == RESERVED_PRIORITY_NUMBER && status != RESERVED) {
            throw new IllegalArgumentException("잘못된 생성자 인자입니다");
        }
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
