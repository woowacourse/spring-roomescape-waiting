package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import roomescape.domain.member.Member;

@Entity
public class ReservationWait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime waitStartAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    public ReservationWait(Long id, LocalDateTime waitStartAt, Member member, Reservation reservation) {
        this.id = id;
        this.waitStartAt = waitStartAt;
        this.member = member;
        this.reservation = reservation;
    }

    protected ReservationWait() {
    }

    public static ReservationWait create(LocalDateTime waitStartAt, Member member, Reservation reservation) {
        return new ReservationWait(null, waitStartAt, member, reservation);
    }
}
