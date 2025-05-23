package roomescape.domain.timeslot;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@ToString
@Entity
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    private TimeSlot(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    protected TimeSlot() {
    }

    public static TimeSlot ofExisting(final long id, final LocalTime startAt) {
        return new TimeSlot(id, startAt);
    }

    public static TimeSlot register(final LocalTime startAt) {
        return new TimeSlot(null, startAt);
    }

    public boolean isTimeBefore(final LocalTime time) {
        return this.startAt.isBefore(time);
    }
}
