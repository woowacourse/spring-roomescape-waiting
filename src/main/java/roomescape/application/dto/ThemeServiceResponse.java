package roomescape.application.dto;

import java.util.List;
import roomescape.domain.entity.Theme;

public record ThemeServiceResponse(
        long id,
        String name,
        String description,
        String thumbnail
) {

    public static ThemeServiceResponse from(Theme theme) {
        return new ThemeServiceResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    public static List<ThemeServiceResponse> from(List<Theme> themes) {
        return themes.stream()
                .map(ThemeServiceResponse::from)
                .toList();
    }
}
