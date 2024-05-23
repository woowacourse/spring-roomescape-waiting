package roomescape.domain;

import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.time.LocalTime;
import java.util.Objects;

@Entity
public class ReservationTime implements Comparable<ReservationTime> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private LocalTime startAt;

    public ReservationTime(LocalTime startAt) {
        this.startAt = startAt;
    }

    protected ReservationTime() {
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public int compareTo(ReservationTime o) {
        return this.startAt.compareTo(o.startAt);
    }
}
