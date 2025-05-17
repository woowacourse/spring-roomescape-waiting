package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;

import java.time.LocalTime;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class ReservationTime {

    private static final int MINUTE_INTERVAL = 30;

    @EmbeddedId
    private final Id id;
    @Embedded
    private StartTime startTime;

    protected ReservationTime() {
        id = Id.issue();
    }

    public static ReservationTime create(final LocalTime startTime) {
        return new ReservationTime(Id.issue(), new StartTime(startTime));
    }

    public static ReservationTime restore(final String id, final LocalTime startTime) {
        return new ReservationTime(Id.create(id), new StartTime(startTime));
    }

    public LocalTime startInterval() {
        return startTime.minusMinutes(MINUTE_INTERVAL);
    }

    public LocalTime endInterval() {
        return startTime.plusMinutes(MINUTE_INTERVAL);
    }

    public LocalTime startTimeValue() {
        return startTime.value();
    }
}
