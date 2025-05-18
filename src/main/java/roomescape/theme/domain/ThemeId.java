package roomescape.theme.domain;

import roomescape.common.domain.DomainId;

public class ThemeId extends DomainId {

    private ThemeId(final Long value) {
        super(value);
    }

    protected ThemeId() {
        this(null);
    }

    public static ThemeId from(final Long id) {
        return new ThemeId(id);
    }
}
