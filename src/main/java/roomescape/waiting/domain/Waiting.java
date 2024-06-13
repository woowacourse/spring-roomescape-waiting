package roomescape.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

@Entity
public class Waiting {

    private static final int NULL_ID = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "id", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "id", nullable = false)
    private Member member;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Waiting() {
    }

    public Waiting(Reservation reservation, Member member, LocalDateTime createdAt) {
        this.id = NULL_ID;
        this.reservation = reservation;
        this.member = member;
        this.createdAt = createdAt;
    }

    public Waiting(long id, Reservation reservation, Member member, LocalDateTime createdAt) {
        this.id = id;
        this.reservation = reservation;
        this.member = member;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }
    
    public Member getMember() {
        return member;
    }
}
