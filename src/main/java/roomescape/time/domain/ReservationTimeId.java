package roomescape.time.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import roomescape.common.domain.DomainId;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class ReservationTimeId {

   private DomainId domainId;

    private ReservationTimeId(final DomainId domainId) {
        this.domainId = domainId;
    }

    public static ReservationTimeId unassigned() {
        return new ReservationTimeId(new DomainId());
    }

    public static ReservationTimeId from(final Long id) {
        return new ReservationTimeId(new DomainId(id));
    }

    public Long getValue() {
        return domainId.getId();
    }
}
