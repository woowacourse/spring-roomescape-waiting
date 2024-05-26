package roomescape.reservation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AvailableTimes {

    private final List<AvailableTime> availableTimes;

    public AvailableTimes(final List<AvailableTime> availableTimes) {
        this.availableTimes = availableTimes;
    }

    public static AvailableTimes of(List<ReservationTime> times, Set<ReservationTime> reservedTimes) {
        List<AvailableTime> result = new ArrayList<>();
        for (ReservationTime time : times) {
            if (reservedTimes.contains(time)) {
                result.add(new AvailableTime(time.getId(), time.getStartAt(), true));
                continue;
            }
            result.add(new AvailableTime(time.getId(), time.getStartAt(), false));
        }
        return new AvailableTimes(result);
    }

    public List<AvailableTime> getAvailableTimes() {
        return availableTimes;
    }
}
