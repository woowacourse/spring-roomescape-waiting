package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record ThemeRankResponse(
        String name,
        String thumbnail,
        String description
) {
    public static ThemeRankResponse from(Theme theme) {
        return new ThemeRankResponse(theme.getName(), theme.getThumbnail(), theme.getDescription());
    }
}
