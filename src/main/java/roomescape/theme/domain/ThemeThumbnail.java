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

import java.net.URI;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class ThemeThumbnail {

    private String value;

    public static ThemeThumbnail from(final String uri) {
        validate(uri);
        return new ThemeThumbnail(uri);
    }

    private static void validate(final String value) {
        Validator.of(ThemeThumbnail.class)
                .validateUriFormat(Fields.value, value, DomainTerm.THEME_THUMBNAIL.label());
    }
}
