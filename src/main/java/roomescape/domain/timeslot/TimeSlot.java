package roomescape.domain.timeslot;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "RESERVATION_TIME")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalTime startAt;

    public TimeSlot(final long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public TimeSlot(final LocalTime startAt) {
        this(0L, startAt);
    }

    public boolean isTimeBefore(final LocalTime time) {
        return this.startAt.isBefore(time);
    }
}
