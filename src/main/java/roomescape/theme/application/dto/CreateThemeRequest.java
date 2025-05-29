package roomescape.theme.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateThemeRequest(
        ThemeName name,
        ThemeDescription description,
        ThemeThumbnail thumbnail
) {

    public CreateThemeRequest {
        validate(name, description, thumbnail);
    }

    public Theme toDomain() {
        return Theme.withoutId(
                name,
                description,
                thumbnail
        );
    }

    private void validate(final ThemeName name, final ThemeDescription description, final ThemeThumbnail thumbnail) {
        Validator.of(CreateThemeRequest.class)
                .validateNotNull(CreateThemeRequest.Fields.name, name, DomainTerm.THEME_NAME.label())
                .validateNotNull(CreateThemeRequest.Fields.description, description, DomainTerm.THEME_DESCRIPTION.label())
                .validateNotNull(CreateThemeRequest.Fields.thumbnail, thumbnail, DomainTerm.THEME_THUMBNAIL.label());
    }
}
