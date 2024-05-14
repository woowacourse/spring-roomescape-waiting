package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    @OneToMany(mappedBy = "reservationTime")
    private Set<Reservation> reservations = new HashSet<>();

    public ReservationTime() {
    }

    public ReservationTime(LocalTime startAt) {
        validateIsNull(startAt);
        this.startAt = startAt;
    }

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    private void validateIsNull(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("값을 입력하지 않았습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
