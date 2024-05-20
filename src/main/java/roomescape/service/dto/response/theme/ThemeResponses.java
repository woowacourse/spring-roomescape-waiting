package roomescape.service.dto.response.theme;

import java.util.List;
import roomescape.domain.Theme;

public record ThemeResponses(List<ThemeResponse> themes) {

    public static ThemeResponses from(List<Theme> themes) {
        List<ThemeResponse> responses = themes.stream()
                .map(ThemeResponse::new)
                .toList();
        return new ThemeResponses(responses);
    }
}
