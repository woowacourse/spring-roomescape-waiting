package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class WaitInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "reservation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Reservation reservation;

    @Column(name = "rank")
    private Long rank;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public WaitInfo(final Long id, final Member member, final Reservation reservation, final Long rank) {
        this.id = id;
        this.member = member;
        this.reservation = reservation;
        this.rank = rank;
    }

    public WaitInfo(final Member member, final Reservation reservation, final Long rank) {
        this(null, member, reservation, rank);
    }

    protected WaitInfo() {
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

    public Long getRank() {
        return rank;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
