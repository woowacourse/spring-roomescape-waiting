package roomescape.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;
    private Boolean alreadyBooked;

    public ReservationTime() {
    }

    public ReservationTime(LocalTime startAt, Boolean alreadyBooked) {
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public boolean isBefore(LocalTime time) {
        return startAt.isBefore(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public Boolean getAlreadyBooked() {
        return alreadyBooked;
    }
}
