package roomescape.dto;

import roomescape.domain.Theme;

import java.util.List;

public record ThemeResponses(
        List<ThemeResponse> themes
) {
    public static ThemeResponses from(List<Theme> themes) {
        return new ThemeResponses(themes.stream()
                .map(ThemeResponse::from)
                .toList());
    }
}
