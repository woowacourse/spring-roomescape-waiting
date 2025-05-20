package roomescape.reservation.time.domain;

import roomescape.common.domain.EntityId;

public class ReservationTimeId extends EntityId {

    private ReservationTimeId(final Long value) {
        super(value);
    }

    public static ReservationTimeId from(final Long id) {
        return new ReservationTimeId(id);
    }
}
