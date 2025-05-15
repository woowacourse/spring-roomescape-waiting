package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class ThemeDescription {

    @Column(name = "description")
    private String value;

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

