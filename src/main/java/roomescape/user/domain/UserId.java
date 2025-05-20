package roomescape.user.domain;

import jakarta.persistence.Embeddable;
import roomescape.common.domain.EntityId;

@Embeddable
public class UserId extends EntityId {

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
