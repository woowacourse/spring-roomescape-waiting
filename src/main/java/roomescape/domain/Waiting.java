package roomescape.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cascade;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Waiting implements Comparable<Waiting> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private LocalDateTime createAt;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Reservation reservation;

    public Waiting(LocalDateTime createAt, Member member, Reservation reservation) {
        this.createAt = createAt;
        this.member = member;
        this.reservation = reservation;
    }

    protected Waiting() {
    }

    public int getRank() {
        if (reservation.hasWaiting(this)) {
            return reservation.rank(this);
        }
        throw new IllegalStateException("대기 순서를 반환할 수 없습니다.");
    }

    public boolean isSameReservationWaiting(Reservation otherReservation) {
        return this.reservation.getDate() == otherReservation.getDate()
                && this.reservation.getTime().equals(otherReservation.getTime())
                && this.reservation.getTheme().equals(otherReservation.getTheme());
    }

    public void delete(){
        reservation.removeWaiting(this);
        member.removeWaiting(this);
    }

    public Member getMember() {
        return member;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Long getId() {
        return id;
    }

    @Override
    public int compareTo(Waiting o) {
        return createAt.compareTo(o.createAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waiting waiting = (Waiting) o;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
