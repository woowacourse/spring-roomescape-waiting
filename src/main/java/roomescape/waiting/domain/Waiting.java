package roomescape.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

@Entity
@Table(name = "waiting")
@EntityListeners(AuditingEntityListener.class)
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member;
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Waiting() {
    }

    public Waiting(Reservation reservation, Member member) {
        this(null, reservation, member);
    }

    public Waiting(Long id, Reservation reservation, Member member) {
        validateDuplicate(reservation, member);
        this.id = id;
        this.reservation = reservation;
        this.member = member;
    }

    private void validateDuplicate(Reservation reservation, Member member) {
        if (member.equals(reservation.getMember())) {
            throw new IllegalArgumentException("자신이 예약한 방탈출에 대해 예약 대기를 할 수 없습니다.");
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Waiting waiting = (Waiting) o;

        return id.equals(waiting.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Waiting{" +
               "id=" + id +
               ", reservation=" + reservation +
               ", member=" + member +
               ", createdAt=" + createdAt +
               '}';
    }
}
