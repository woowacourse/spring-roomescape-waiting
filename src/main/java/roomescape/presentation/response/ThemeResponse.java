package roomescape.presentation.response;

import java.util.List;
import roomescape.domain.theme.Theme;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnail
) {

    public static List<ThemeResponse> fromThemes(final List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::fromTheme)
                .toList();
    }

    public static ThemeResponse fromTheme(final Theme theme) {
        return new ThemeResponse(
                theme.id(),
                theme.name(),
                theme.description(),
                theme.thumbnail()
        );
    }
}
