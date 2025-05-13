package roomescape.user.domain;

import roomescape.common.domain.DomainId;

public class UserId extends DomainId {

    protected UserId(final Long value, final boolean assigned) {
        super(value, assigned);
    }

    public static UserId unassigned() {
        return new UserId(null, false);
    }

    public static UserId from(final Long id) {
        return new UserId(id, true);
    }
}
