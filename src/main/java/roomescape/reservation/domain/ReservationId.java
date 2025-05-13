package roomescape.reservation.domain;

import lombok.EqualsAndHashCode;
import roomescape.common.domain.DomainId;

@EqualsAndHashCode
public class ReservationId {

    private final DomainId domainId;

    private ReservationId(final DomainId domainId) {
        this.domainId = domainId;
    }

    public static ReservationId unassigned() {
        return new ReservationId(DomainId.unassigned());
    }

    public static ReservationId from(final Long id) {
        return new ReservationId(DomainId.assigned(id));
    }

    public Long getValue() {
        return domainId.getValue();
    }
}
