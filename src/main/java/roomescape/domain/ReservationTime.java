package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;

@Entity
public class ReservationTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    public ReservationTime() {
    }

    public ReservationTime(LocalTime startAt) {
        validateTime(startAt);
        this.startAt = startAt;
    }

    private void validateTime(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시간이 유효하지 않습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

}
