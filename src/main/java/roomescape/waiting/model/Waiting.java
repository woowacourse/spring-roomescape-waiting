package roomescape.waiting.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import roomescape.common.model.BaseEntity;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;

@Entity
public class Waiting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    public Waiting(final Reservation reservation, final Member member) {
        this.id = null;
        this.reservation = reservation;
        this.member = member;
    }

    protected Waiting() {
    }

    public boolean isNotSameMember(final Long memberId) {
        return member.isNotSameMember(memberId);
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
