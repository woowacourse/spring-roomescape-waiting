package roomescape.domain.reservation;

import jakarta.persistence.*;
import roomescape.domain.user.Member;

@Entity
@Table
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    @ManyToOne
    private ReservationInfo reservationInfo;

    protected Reservation() {
    }

    public Reservation(final Member member, final ReservationInfo reservationInfo) {
        this.member = member;
        this.reservationInfo = reservationInfo;
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
}
