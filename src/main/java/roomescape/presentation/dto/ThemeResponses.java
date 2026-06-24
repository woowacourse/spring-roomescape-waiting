package roomescape.presentation.dto;

import java.util.List;
import roomescape.domain.Theme;

public record ThemeResponses(
        List<ThemeResponse> themes
) {
    public static ThemeResponses from(List<Theme> themes) {
        List<ThemeResponse> themeResponses = themes.stream()
                .map(ThemeResponse::from)
                .toList();
        return new ThemeResponses(themeResponses);
    }
}
