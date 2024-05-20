package roomescape.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

@Entity
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Waiting(Reservation reservation, Member member) {
        this.reservation = reservation;
        this.member = member;
        this.createdAt = LocalDateTime.now();
    }

    protected Waiting() {
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
