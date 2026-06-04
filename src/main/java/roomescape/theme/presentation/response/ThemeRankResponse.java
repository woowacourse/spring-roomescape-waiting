package roomescape.theme.presentation.response;

import roomescape.theme.domain.ThemeRankResult;

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
