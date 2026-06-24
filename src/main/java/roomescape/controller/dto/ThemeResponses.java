package roomescape.controller.dto;

import java.util.List;
import roomescape.domain.theme.Theme;

public record ThemeResponses(
        List<ThemeResponse> themeResponses
) {

    public static ThemeResponses from(List<Theme> themes) {
        return new ThemeResponses(
                themes.stream()
                        .map(ThemeResponse::from)
                        .toList()
        );
    }
}
