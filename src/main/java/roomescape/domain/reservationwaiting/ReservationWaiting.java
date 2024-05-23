package roomescape.domain.reservationwaiting;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "member_id"}))
public class ReservationWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    protected ReservationWaiting() {
    }

    public ReservationWaiting(Reservation reservation, Member member) {
        this(null, reservation, member);
    }

    private ReservationWaiting(Long id, Reservation reservation, Member member) {
        this.id = id;
        this.reservation = reservation;
        this.member = member;
    }

    public boolean isSameMember(Member member) {
        return this.member.equals(member);
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Member getMember() {
        return member;
    }
}
