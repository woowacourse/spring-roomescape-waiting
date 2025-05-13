package roomescape.theme.domain;

import lombok.EqualsAndHashCode;
import roomescape.common.domain.DomainId;

@EqualsAndHashCode
public class ThemeId {

    private final DomainId domainId;

    private ThemeId(final DomainId domainId) {
        this.domainId = domainId;
    }

    public static ThemeId unassigned() {
        return new ThemeId(DomainId.unassigned());
    }

    public static ThemeId from(final Long id) {
        return new ThemeId(DomainId.assigned(id));
    }

    public Long getValue() {
        return domainId.getValue();
    }
}
