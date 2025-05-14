package roomescape.time.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;

@Entity
@Embeddable
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalTime startAt;

    public ReservationTime() {

    }

    public ReservationTime(Long id, LocalTime time) {
        this.id = id;
        this.startAt = time;
    }

    public static ReservationTime create(LocalTime time) {
        return new ReservationTime(null, time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
