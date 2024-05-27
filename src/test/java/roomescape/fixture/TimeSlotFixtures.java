package roomescape.fixture;

import java.time.LocalTime;
import roomescape.domain.TimeSlot;

public class TimeSlotFixtures {

    private TimeSlotFixtures() {
    }

    public static TimeSlot createTimeSlot(LocalTime startAt) {
        return new TimeSlot(startAt);
    }
}
