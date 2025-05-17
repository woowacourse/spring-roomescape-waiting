package roomescape.theme.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.theme.application.dto.CreateThemeServiceRequest;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateThemeWebRequest(
        String name,
        String description,
        String thumbnail
) {

    public CreateThemeWebRequest {
        validate(name, description, thumbnail);
    }

    public CreateThemeServiceRequest toServiceRequest() {
        return new CreateThemeServiceRequest(
                ThemeName.from(name),
                ThemeDescription.from(description),
                ThemeThumbnail.from(thumbnail));
    }

    private void validate(final String name, final String description, final String thumbnail) {
        Validator.of(CreateThemeWebRequest.class)
                .validateNotNull(Fields.name, name, DomainTerm.THEME_NAME.label())
                .validateNotNull(Fields.description, description, DomainTerm.THEME_DESCRIPTION.label())
                .validateNotNull(Fields.thumbnail, thumbnail, DomainTerm.THEME_THUMBNAIL.label());
    }
}
