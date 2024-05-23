package roomescape.domain.waiting;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    protected Waiting() {
    }

    // TODO : isNotOwner를 생성자 내부로 이동
    public Waiting(Member member, Reservation reservation) {
        this.member = member;
        this.reservation = reservation;
    }

    public boolean isNotOwner(Member member) {
        return !this.member.equals(member);
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
