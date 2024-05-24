package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.member.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime createdAt;

    public Waiting(Reservation reservation, Member member, LocalDateTime createdAt) {
        this.reservation = reservation;
        this.member = member;
        this.createdAt = createdAt;
    }

    public Waiting() {
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Waiting waiting = (Waiting) o;
        return Objects.equals(id, waiting.id) && Objects.equals(reservation, waiting.reservation)
                && Objects.equals(member, waiting.member) && Objects.equals(createdAt,
                waiting.createdAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(reservation);
        result = 31 * result + Objects.hashCode(member);
        result = 31 * result + Objects.hashCode(createdAt);
        return result;
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
