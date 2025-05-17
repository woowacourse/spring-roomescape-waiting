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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class ThemeName {

    @Column(name = "name", nullable = false)
    private String value;

    public static ThemeName from(final String name) {
        validate(name);
        return new ThemeName(name);
    }

    private static void validate(final String value) {
        Validator.of(ThemeName.class)
                .notNullField(Fields.value, value)
                .notBlankField(Fields.value, value.strip());
    }
}
