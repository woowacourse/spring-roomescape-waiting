package roomescape.presentation.theme.response;

import java.util.List;
import roomescape.domain.theme.Theme;

public record AdminThemesResponse(
    List<ThemePayload> themes
) {

    public static AdminThemesResponse from(List<Theme> themes) {
        List<ThemePayload> payloads = themes.stream()
                .map(ThemePayload::from)
                .toList();

        return new AdminThemesResponse(payloads);
    }

    private record ThemePayload(
            Long id,
            String name,
            String content,
            String url
    ) {

        private static ThemePayload from(Theme theme) {
            return new ThemePayload(
                    theme.getId(),
                    theme.getName(),
                    theme.getDescription(),
                    theme.getThumbnailUrl()
            );
        }
    }
}
