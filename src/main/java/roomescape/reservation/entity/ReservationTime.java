package roomescape.reservation.entity;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationTime {

    private static final LocalTime RUNNING_TIME = LocalTime.of(2, 0);
    private static final LocalTime OPERATING_START = LocalTime.of(10, 0);
    private static final LocalTime OPERATING_END = LocalTime.of(22, 0);

    private final Long id;
    private LocalTime startAt;

    public boolean isDuplicatedWith(ReservationTime other) {
        LocalTime otherStartAt = other.startAt;
        int interval = Math.abs(otherStartAt.toSecondOfDay() - startAt.toSecondOfDay());
        return interval < RUNNING_TIME.toSecondOfDay();
    }

    public boolean isAvailable() {
        return !(startAt.isBefore(OPERATING_START) || startAt.isAfter(OPERATING_END));
    }

    public String getFormattedTime() {
        return startAt.toString();
    }
}
