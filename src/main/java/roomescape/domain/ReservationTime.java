package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "time")
    private List<Reservation> reservations = new ArrayList<>();

    private ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    protected ReservationTime() {
    }

    public static ReservationTime of(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime withoutId(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public boolean hasReservationOn(LocalDate date, Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.isAlreadyBookedTime(date, themeId, this.id));
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
        reservation.setTime(this);
    }

    void removeReservation(Reservation reservation) {
        this.reservations.remove(reservation);
        reservation.setTime(null);
    }

    public boolean compareEqualId(ReservationTime time) {
        return this.id.equals(time.id);
    }
}
