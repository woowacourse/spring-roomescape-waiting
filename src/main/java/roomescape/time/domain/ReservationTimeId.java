package roomescape.time.domain;

import roomescape.common.domain.DomainId;

public class ReservationTimeId extends DomainId {

    private ReservationTimeId(final Long value) {
        super(value);
    }

    public static ReservationTimeId from(final Long id) {
        return new ReservationTimeId(id);
    }
}
