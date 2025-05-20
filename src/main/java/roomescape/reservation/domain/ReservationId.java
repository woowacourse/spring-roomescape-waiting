package roomescape.reservation.domain;

import roomescape.common.domain.EntityId;

public class ReservationId extends EntityId {

    private ReservationId(final Long value) {
        super(value);
    }

    public static ReservationId from(final Long id) {
        return new ReservationId(id);
    }
}
