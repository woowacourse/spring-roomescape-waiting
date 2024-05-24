package roomescape.service.response;

import roomescape.domain.Theme;

public record ThemeDto(Long id, String name, String description, String thumbnail) {

    public static ThemeDto from(Theme theme) {
        return new ThemeDto(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
