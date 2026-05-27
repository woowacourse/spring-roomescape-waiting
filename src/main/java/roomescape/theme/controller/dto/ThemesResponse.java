package roomescape.theme.controller.dto;

import java.util.List;

public record ThemesResponse(List<ThemeResponse> themes) {

    public static ThemesResponse from(List<ThemeResponse> themes) {
        return new ThemesResponse(themes);
    }
}
