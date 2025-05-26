package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.NoSuchElementException;
import roomescape.member.domain.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Reservation reservation;
    @ManyToOne
    private Member member;

    protected Waiting() {
    }

    public Waiting(Long id, Reservation reservation, Member member) {
        validateReservationNotNull(reservation);
        this.id = id;
        this.reservation = reservation;
        this.member = member;
    }

    public Waiting(Reservation reservation, Member member) {
        this(null, reservation, member);
    }

    public static Waiting create(Reservation reservation, Member member) {
        return new Waiting(null, reservation, member);
    }

    private void validateReservationNotNull(Reservation reservation) {
        if (reservation == null) {
            throw new NoSuchElementException("[ERROR] 예약 정보를 찾지 못했습니다.");
        }
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
