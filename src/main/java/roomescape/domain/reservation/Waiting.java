package roomescape.domain.reservation;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import roomescape.domain.user.Member;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationInfo reservationInfo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    protected Waiting() {

    }

    public Waiting(final Member member, final ReservationInfo reservationInfo) {
        this.member = member;
        this.reservationInfo = reservationInfo;
    }

    public Reservation toReservation() {
        return new Reservation(member, reservationInfo);
    }

    public boolean isEqualMemberId(final long memberId){
        return this.member.isEqualId(memberId);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationInfo getReservationInfo() {
        return reservationInfo;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final Waiting waiting)) return false;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Waiting{" +
                "id=" + id +
                ", member=" + member +
                ", reservationInfo=" + reservationInfo +
                ", createdDate=" + createdDate +
                '}';
    }
}
