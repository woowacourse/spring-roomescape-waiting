package roomescape.theme.dto.response;

import roomescape.theme.entity.Theme;

public class ThemeResponse {

    public record ThemeCreateResponse(
            Long id,
            String name,
            String description,
            String thumbnail
    ) {
        public static ThemeCreateResponse from(Theme theme) {
            return new ThemeCreateResponse(
                    theme.getId(),
                    theme.getName(),
                    theme.getDescription(),
                    theme.getThumbnail()
            );
        }
    }

    public record ThemeReadResponse(
            Long id,
            String name,
            String description,
            String thumbnail
    ) {
        public static ThemeReadResponse from(Theme theme) {
            return new ThemeReadResponse(
                    theme.getId(),
                    theme.getName(),
                    theme.getDescription(),
                    theme.getThumbnail()
            );
        }
    }
}
