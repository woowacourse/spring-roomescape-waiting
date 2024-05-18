package roomescape.reservation.controller.dto.response;

import java.util.List;
import roomescape.reservation.domain.Theme;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnail
) {

    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getThemeNameValue(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    public static List<ThemeResponse> list(final List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
