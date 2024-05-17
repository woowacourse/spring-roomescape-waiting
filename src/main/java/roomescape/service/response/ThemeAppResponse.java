package roomescape.service.response;

import roomescape.domain.Theme;

public record ThemeAppResponse(Long id, String name, String description, String thumbnail) {

    public static ThemeAppResponse from(Theme theme) {
        return new ThemeAppResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
