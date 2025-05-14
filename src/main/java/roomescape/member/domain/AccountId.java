package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import roomescape.common.domain.DomainId;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class AccountId {

    private DomainId domainId;

    private AccountId(DomainId domainId) {
        this.domainId = domainId;
    }

    public static AccountId unassigned() {
        return new AccountId(new DomainId());
    }

    public static AccountId from(final Long id) {
        return new AccountId(new DomainId(id));
    }

    public Long getValue() {
        return domainId.getId();
    }
}
