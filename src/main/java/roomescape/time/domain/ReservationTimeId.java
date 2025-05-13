package roomescape.time.domain;

import lombok.EqualsAndHashCode;
import roomescape.common.domain.DomainId;

@EqualsAndHashCode
public class ReservationTimeId {

   private final DomainId domainId;

    private ReservationTimeId(final DomainId domainId) {
        this.domainId = domainId;
    }

    public static ReservationTimeId unassigned() {
        return new ReservationTimeId(DomainId.unassigned());
    }

    public static ReservationTimeId from(final Long id) {
        return new ReservationTimeId(DomainId.assigned(id));
    }

    public Long getValue() {
        return domainId.getValue();
    }
}
