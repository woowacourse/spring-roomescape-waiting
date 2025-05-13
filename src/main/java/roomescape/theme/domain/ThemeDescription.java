package roomescape.theme.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class ThemeDescription {

    private final String value;

    public static ThemeDescription from(final String description) {
        validate(description);
        return new ThemeDescription(description);
    }

    private static void validate(final String value) {
        Validator.of(ThemeDescription.class)
                .notNullField(Fields.value, value)
                .notBlankField(Fields.value, value.strip());
    }
}

