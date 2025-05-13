package roomescape.theme.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

import java.net.URI;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class ThemeThumbnail {

    private final URI value;

    public static ThemeThumbnail from(final String uri) {
        validate(uri);
        return new ThemeThumbnail(URI.create(uri));
    }

    private static void validate(final String value) {
        Validator.of(ThemeThumbnail.class)
                .validateUriFormat(Fields.value, value, DomainTerm.THEME_THUMBNAIL.label());
    }
}
