package roomescape.time.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import roomescape.reservation.domain.Reservation;

@Entity
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;
    @OneToMany(mappedBy = "time")
    private List<Reservation> reservations;

    public ReservationTime(LocalTime startAt) {
        this.id = null;
        this.startAt = Objects.requireNonNull(startAt);
    }

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = Objects.requireNonNull(id);
        this.startAt = Objects.requireNonNull(startAt);
    }

    protected ReservationTime() {
    }

    public ReservationTime withId(Long id) {
        return new ReservationTime(id, this.startAt);
    }

    public boolean isBefore(LocalTime currentTime) {
        return startAt.isBefore(currentTime);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime time = (ReservationTime) o;
        return Objects.equals(id, time.id) && Objects.equals(startAt, time.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
