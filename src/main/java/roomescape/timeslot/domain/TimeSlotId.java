package roomescape.timeslot.domain;

import roomescape.common.domain.EntityId;

public class TimeSlotId extends EntityId {

    private TimeSlotId(final Long value) {
        super(value);
    }

    public static TimeSlotId from(final Long id) {
        return new TimeSlotId(id);
    }
}
