package roomescape.presentation.response;

import java.util.List;
import roomescape.domain.theme.Theme;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnail
) {

    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(
                theme.id(),
                theme.name(),
                theme.description(),
                theme.thumbnail()
        );
    }

    public static List<ThemeResponse> from(final List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
