package roomescape.member.domain;

import lombok.EqualsAndHashCode;
import roomescape.common.domain.DomainId;

@EqualsAndHashCode
public class MemberId {

    private final DomainId domainId;

    private MemberId(DomainId domainId) {
        this.domainId = domainId;
    }

    public static MemberId unassigned() {
        return new MemberId(DomainId.unassigned());
    }

    public static MemberId from(final Long id) {
        return new MemberId(DomainId.assigned(id));
    }

    public Long getValue() {
        return domainId.getValue();
    }
}
