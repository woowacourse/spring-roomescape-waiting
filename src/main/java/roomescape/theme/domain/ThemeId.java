package roomescape.theme.domain;

import roomescape.common.domain.DomainId;

public class ThemeId extends DomainId {

    private ThemeId(final Long value, final boolean assigned) {
        super(value, assigned);
    }

    public static ThemeId unassigned() {
        return new ThemeId(null, false);
    }

    public static ThemeId from(final Long id) {
        return new ThemeId(id, true);
    }
}
