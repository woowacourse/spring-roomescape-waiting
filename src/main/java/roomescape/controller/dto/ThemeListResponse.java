package roomescape.controller.dto;

import java.util.List;

public record ThemeListResponse(List<ThemeResponse> themes) {

    public static ThemeListResponse from(List<ThemeResponse> themes) {
        return new ThemeListResponse(themes);
    }
}
