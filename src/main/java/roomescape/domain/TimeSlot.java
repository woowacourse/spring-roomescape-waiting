package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalTime;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

@Entity
public class TimeSlot {
    @Id
    private Long id;
    private LocalTime startAt;

    public TimeSlot() {
    }

    public TimeSlot(Long id, LocalTime startAt) {
        if (startAt == null) {
            throw new EmptyValueNotAllowedException("startAt");
        }

        this.id = id;
        this.startAt = startAt;
    }

    public TimeSlot(Long id, String startAt) {
        this(id, LocalTime.parse(startAt));
    }

    public TimeSlot(String startAt) {
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
