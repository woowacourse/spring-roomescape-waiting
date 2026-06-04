package roomescape.service.dto;

import roomescape.domain.ReservationTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvailableTimes {

    private final List<TimeAvailability> timeAvailabilities;

    private AvailableTimes(List<TimeAvailability> timeAvailabilities) {
        this.timeAvailabilities = timeAvailabilities;
    }

    public static AvailableTimes from(List<ReservationTime> times, List<Long> reservedTimeIds) {
        Set<Long> reservedIds = new HashSet<>(reservedTimeIds);
        List<TimeAvailability> timeAvailabilities = times.stream()
                .map(time -> TimeAvailability.from(time, !reservedIds.contains(time.getId())))
                .toList();

        return new AvailableTimes(timeAvailabilities);
    }

    public List<TimeAvailability> values() {
        return timeAvailabilities;
    }
}
