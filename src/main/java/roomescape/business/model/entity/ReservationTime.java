package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
@Table(name = "reservation_time")
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
