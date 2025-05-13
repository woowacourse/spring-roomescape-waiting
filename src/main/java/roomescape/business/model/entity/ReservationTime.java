package roomescape.business.model.entity;

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
public class ReservationTime {

    private static final int MINUTE_INTERVAL = 30;

    private final Id id;
    private final StartTime startTime;

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
