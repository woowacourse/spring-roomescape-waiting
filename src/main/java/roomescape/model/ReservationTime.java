package roomescape.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    @OneToMany(mappedBy = "time")
    private List<Reservation> reservations = new ArrayList<>();

    protected ReservationTime() {
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return id.equals(that.id) && Objects.equals(startAt, that.startAt) && Objects.equals(reservations, that.reservations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt, reservations);
    }

    @Override
    public String toString() {
        return "ReservationTime{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}
