package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class WaitInfo {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    @Column(name = "created_at")
    private String createdAt;

    public WaitInfo(final Member member, final Reservation reservation, final Long rank) {
        this.member = member;
        this.reservation = reservation;
        this.rank = rank;
        this.createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setRank(final Long rank) {
        this.rank = rank;
    }
}
