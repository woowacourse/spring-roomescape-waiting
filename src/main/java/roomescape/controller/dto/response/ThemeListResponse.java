package roomescape.controller.dto.response;

import java.util.List;
import roomescape.domain.Theme;

public record ThemeListResponse(
        List<ThemeResponse> items
) {
    public static ThemeListResponse from(List<Theme> themes) {
        return new ThemeListResponse(themes.stream()
                .map(ThemeResponse::from)
                .toList());
    }
}
