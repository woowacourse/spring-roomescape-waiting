package roomescape.domain.waiting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

@Entity
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Reservation reservation;
    @ManyToOne(optional = false)
    private Member member;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private WaitingStatus waitingStatus;

    protected Waiting() {
    }

    public Waiting(Long id, Reservation reservation, Member member, LocalDateTime createdAt) {
        this.id = id;
        this.reservation = reservation;
        this.member = member;
        this.createdAt = createdAt;
        this.waitingStatus = WaitingStatus.WAITING;
    }

    public Waiting(Reservation reservation, Member member, LocalDateTime createdAt) {
        this(null, reservation, member, createdAt);
    }

    public boolean isOwnedBy(long memberId) {
        return member.hasId(memberId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Waiting that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public WaitingStatus getWaitingStatus() {
        return waitingStatus;
    }
}
