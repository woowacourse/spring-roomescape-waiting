package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class ReservationTime {
    private static final long TRANSIENT = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalTime startAt;

    protected ReservationTime() {
    }

    private ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = Objects.requireNonNull(startAt);
    }

    public static ReservationTime of(long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime of(LocalTime startAt) {
        return new ReservationTime(TRANSIENT, startAt);
    }

    public ReservationTime withId(long id) {
        return new ReservationTime(id, startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
