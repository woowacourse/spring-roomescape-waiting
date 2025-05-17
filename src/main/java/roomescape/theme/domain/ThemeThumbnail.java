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
public class ThemeThumbnail {

    @Column(name = "thumbnail", nullable = false)
    private String value;

    public static ThemeThumbnail from(final String url) {
        validate(url);
        return new ThemeThumbnail(url);
    }

    private static void validate(final String value) {
        Validator.of(ThemeThumbnail.class)
                .notNullField(Fields.value, value)
                .notBlankField(Fields.value, value.strip());
    }
}
