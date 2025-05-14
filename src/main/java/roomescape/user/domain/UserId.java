package roomescape.user.domain;

import jakarta.persistence.Embeddable;
import roomescape.common.domain.DomainId;

@Embeddable
public class UserId extends DomainId {

    protected UserId() {
        super();
    }

    protected UserId(final Long value) {
        super(value);
    }

    public static UserId from(final Long id) {
        return new UserId(id);
    }
}
