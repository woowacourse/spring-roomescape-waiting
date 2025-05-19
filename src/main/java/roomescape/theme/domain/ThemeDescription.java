package roomescape.theme.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class ThemeDescription {

    private String value;

    public static ThemeDescription from(final String description) {
        validate(description);
        return new ThemeDescription(description);
    }

    private static void validate(final String value) {
        Validator.of(ThemeDescription.class)
                .validateNotBlank(Fields.value, value, DomainTerm.THEME_DESCRIPTION.label());
    }
}

