package roomescape.presentation.theme.response;

import roomescape.domain.theme.ThemeRankResult;

public record ThemeRankResponse(
        Long id,
        String themeName,
        String url,
        Integer rank
) {

    public static ThemeRankResponse from(ThemeRankResult themeRankResult) {
        return new ThemeRankResponse(
                themeRankResult.id(),
                themeRankResult.name(),
                themeRankResult.url(),
                themeRankResult.rank()
        );
    }
}
