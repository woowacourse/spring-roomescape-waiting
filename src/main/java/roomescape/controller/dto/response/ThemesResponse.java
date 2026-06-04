package roomescape.controller.dto.response;

import roomescape.domain.Theme;

import java.util.List;

public record ThemesResponse(
        List<ThemeResponse> themes
) {

    public static ThemesResponse from(List<Theme> themes) {
        return new ThemesResponse(themes.stream()
                .map(ThemeResponse::from)
                .toList()
        );
    }
}
