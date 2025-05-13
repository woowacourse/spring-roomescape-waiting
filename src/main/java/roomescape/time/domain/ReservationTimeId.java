package roomescape.time.domain;

import roomescape.common.domain.DomainId;

public class ReservationTimeId extends DomainId {

    private ReservationTimeId(final Long value, final boolean assigned) {
        super(value, assigned);
    }

    public static ReservationTimeId unassigned() {
        return new ReservationTimeId(null, false);
    }

    public static ReservationTimeId from(final Long id) {
        return new ReservationTimeId(id, true);
    }
}
