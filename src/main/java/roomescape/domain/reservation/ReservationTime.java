package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

@Table(name = "reservation_time")
@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "start_at")
    private LocalTime startAt;

    public ReservationTime(String startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, String startAt) {
        this.id = id;
        this.startAt = LocalTime.parse(startAt);
    }

    protected ReservationTime() {
    }

    public boolean isBeforeNow() {
        return startAt.isBefore(LocalTime.now());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return this.startAt;
    }
}
