package roomescape.presentation.dto.response;

import roomescape.domain.Theme;

import java.util.List;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public static List<ThemeResponse> from(List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
