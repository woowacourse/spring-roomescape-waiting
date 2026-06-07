package roomescape.application.theme.response;

import java.util.List;
import roomescape.domain.theme.Theme;

public record ThemesResponse(
        List<ThemeResponse> themes
) {

    public static ThemesResponse from(List<Theme> themes) {
        List<ThemeResponse> payloads = themes.stream()
                .map(ThemeResponse::from)
                .toList();

        return new ThemesResponse(payloads);
    }

    private record ThemeResponse(
            Long id,
            String name,
            String content,
            String url
    ) {
        private static ThemeResponse from(Theme theme) {
            return new ThemeResponse(
                    theme.getId(),
                    theme.getName(),
                    theme.getDescription(),
                    theme.getThumbnailUrl()
            );
        }
    }
}
