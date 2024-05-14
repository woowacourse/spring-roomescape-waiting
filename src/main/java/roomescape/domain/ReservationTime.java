package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalTime;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

@Entity
public class ReservationTime {
    @Id
    private Long id;
    private LocalTime startAt;

    public ReservationTime() {
    }

    public ReservationTime(Long id, LocalTime startAt) {
        if (startAt == null) {
            throw new EmptyValueNotAllowedException("startAt");
        }

        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(Long id, String startAt) {
        this(id, LocalTime.parse(startAt));
    }

    public ReservationTime(String startAt) {
        this(null, startAt);
    }

    public boolean isTimeBeforeNow() {
        return startAt.isBefore(LocalTime.now());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
