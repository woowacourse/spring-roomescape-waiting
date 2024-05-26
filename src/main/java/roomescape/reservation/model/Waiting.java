package roomescape.reservation.model;

import jakarta.persistence.*;

import roomescape.member.model.Member;

@Entity
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Reservation reservation;

    @ManyToOne
    Member member;

    protected Waiting() {
    }

    public Waiting(Reservation reservation, Member member) {
        this.reservation = reservation;
        this.member = member;
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
