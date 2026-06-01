package roomescape.controller.dto.response;

import roomescape.domain.theme.Theme;

import java.util.List;

public class ThemeResponses {
    private final List<ThemeResponse> themes;

    public ThemeResponses(List<ThemeResponse> themes) {
        this.themes = themes;
    }

    public static ThemeResponses toDto(List<Theme> themes) {
        return new ThemeResponses(themes.stream()
                .map(ThemeResponse::toDto)
                .toList());
    }

    public List<ThemeResponse> getThemes() {
        return themes;
    }
}
