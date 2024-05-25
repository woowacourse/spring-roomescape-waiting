package roomescape.service.dto.response;

import roomescape.domain.theme.Theme;

public record ThemeResponse(Long id, String name, String description, String thumbnail) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getRawName(), theme.getDescription(), theme.getThumbnail());
    }
}
