package roomescape.theme.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import roomescape.common.domain.DomainId;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class ThemeId {

    private DomainId domainId;

    private ThemeId(final DomainId domainId) {
        this.domainId = domainId;
    }

    public static ThemeId unassigned() {
        return new ThemeId(new DomainId());
    }

    public static ThemeId from(final Long id) {
        return new ThemeId(new DomainId(id));
    }

    public Long getValue() {
        return domainId.getId();
    }
}
