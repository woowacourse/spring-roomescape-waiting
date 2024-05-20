package roomescape.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Objects;

@Getter
@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    protected ReservationTime() {
    }

    @Builder
    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationTime reservationTime = (ReservationTime) o;
        return Objects.equals(id, reservationTime.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
