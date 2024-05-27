package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class ReservationWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @OneToOne
    private Reservation reservation;

    protected ReservationWaiting() {
    }

    public ReservationWaiting(Member member, Reservation reservation) {
        this.member = member;
        this.reservation = reservation;
    }

    public ReservationWaiting(Long id, Member member, Reservation reservation) {
        this.id = id;
        this.member = member;
        this.reservation = reservation;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
