package roomescape.domain.time.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "reservation_time")
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalTime startAt;

    protected ReservationTime() {

    }

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    @JsonFormat(pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public String toString() {
        return "ReservationTime{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}
