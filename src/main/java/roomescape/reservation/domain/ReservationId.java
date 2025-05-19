package roomescape.reservation.domain;

import roomescape.common.domain.DomainId;

public class ReservationId extends DomainId {

    private ReservationId(final Long value) {
        super(value);
    }

    public static ReservationId from(final Long id) {
        return new ReservationId(id);
    }
}
