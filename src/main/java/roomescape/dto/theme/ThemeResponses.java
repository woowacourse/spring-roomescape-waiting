package roomescape.dto.theme;

import java.util.List;
import roomescape.domain.Theme;

public record ThemeResponses(
        List<ThemeResponse> themes,
        boolean hasNext
) {
    public static ThemeResponses of(List<Theme> themes, boolean hasNext) {
        return new ThemeResponses(
                themes.stream().map(ThemeResponse::from).toList(),
                hasNext
        );
    }
}
