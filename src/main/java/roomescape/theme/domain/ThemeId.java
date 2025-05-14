package roomescape.theme.domain;

import jakarta.persistence.Embeddable;
import roomescape.common.domain.DomainId;

@Embeddable
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
