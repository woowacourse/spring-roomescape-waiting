package roomescape.dto;

import java.util.List;
import roomescape.domain.Theme;

public record ThemeResponses(
        List<ThemeResponse> themes
) {
    public static ThemeResponses from(List<Theme> themes) {
        return new ThemeResponses(themes.stream()
                .map(ThemeResponse::from)
                .toList());
    }
}
