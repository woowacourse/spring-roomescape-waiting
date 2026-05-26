package roomescape.domain.theme.dto;

import roomescape.domain.theme.Theme;

public record ThemeRankResponse(
    Long id,
    String themeName,
    String url
) {

    public static ThemeRankResponse from(Theme theme) {
        return new ThemeRankResponse(
            theme.getId(),
            theme.getName(),
            theme.getUrl()
        );
    }
}
