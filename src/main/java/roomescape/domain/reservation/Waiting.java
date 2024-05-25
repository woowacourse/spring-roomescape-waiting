package roomescape.domain.reservation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.member.Member;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Waiting implements Comparable<Waiting> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createAt;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    public Waiting(LocalDateTime createAt, Member member, Reservation reservation) {
        this.createAt = createAt;
        this.member = member;
        this.reservation = reservation;
    }

    protected Waiting() {
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
        if (o == null ||!(o instanceof Waiting)) return false;
        Waiting waiting = (Waiting) o;
        return Objects.equals(this.getId(), waiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
