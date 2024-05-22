package roomescape.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

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

    public boolean isSameReservationWaiting(Reservation otherReservation) {
        return this.reservation.getDate() == otherReservation.getDate()
                && this.reservation.getTime().equals(otherReservation.getTime())
                && this.reservation.getTheme().equals(otherReservation.getTheme());
    }

    public Member getMember() {
        return member;
    }

    public Reservation getReservation() {
        return reservation;
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
