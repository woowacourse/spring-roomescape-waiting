package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import roomescape.common.domain.DomainId;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class MemberId {

    private DomainId domainId;

    private MemberId(DomainId domainId) {
        this.domainId = domainId;
    }

    public static MemberId unassigned() {
        return new MemberId(new DomainId());
    }

    public static MemberId from(final Long id) {
        return new MemberId(new DomainId(id));
    }

    public Long getValue() {
        return domainId.getId();
    }
}
