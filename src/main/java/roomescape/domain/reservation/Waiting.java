package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import roomescape.domain.member.Member;

@Entity
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE waiting SET is_deleted = true where id = ?")
public class Waiting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private boolean isDeleted = false;

    public Waiting(Member member, Reservation reservation) {
        this(null, member, reservation);
    }

    public Waiting(Long id, Member member, Reservation reservation) {
        this.id = id;
        this.member = member;
        this.reservation = reservation;
    }

    protected Waiting() {
    }

    public boolean isMember(Member member) {
        return this.member.equals(member);
    }

    public boolean isPriority(Waiting other) {
        return this.reservation.equals(other.reservation) && this.id < other.id;
    }

    public boolean isMemberId(Long id) {
        return member.getId().equals(id);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Waiting waiting)) {
            return false;
        }
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
