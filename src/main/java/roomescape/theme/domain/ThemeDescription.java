package roomescape.theme.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class ThemeDescription {

    private final String value;

    public static ThemeDescription from(final String description) {
        validate(description);
        return new ThemeDescription(description);
    }

    private static void validate(final String value) {
        Validator.of(ThemeDescription.class)
                .validateNotBlank(Fields.value, value, DomainTerm.THEME_DESCRIPTION.label());
    }
}

