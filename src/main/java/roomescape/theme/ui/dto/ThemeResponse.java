package roomescape.theme.ui.dto;

import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.theme.domain.Theme;

import java.util.List;

@FieldNameConstants
public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public ThemeResponse {
        validate(id, name, description, thumbnail);
    }

    public static ThemeResponse from(final Theme domain) {
        return new ThemeResponse(
                domain.getId().getValue(),
                domain.getName().getValue(),
                domain.getDescription().getValue(),
                String.valueOf(domain.getThumbnail().getValue()));
    }

    public static List<ThemeResponse> from(final List<Theme> domains) {
        return domains.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    private static void validate(final Long id,
                                 final String name,
                                 final String description,
                                 final String thumbnail) {
        Validator.of(ThemeResponse.class)
                .validateNotNull(Fields.id, id, DomainTerm.THEME_ID.label())
                .validateNotNull(Fields.name, name, DomainTerm.THEME_NAME.label())
                .validateNotNull(Fields.description, description, DomainTerm.THEME_DESCRIPTION.label())
                .validateNotNull(Fields.thumbnail, thumbnail, DomainTerm.THEME_THUMBNAIL.label());
    }
}
