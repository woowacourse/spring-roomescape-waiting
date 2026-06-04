package roomescape.domain.theme;

public record ThemeRankResult(
        Long id,
        String name,
        String url,
        Integer rank
) {

    public static ThemeRankResult of(Theme theme, int rank) {
        return new ThemeRankResult(
                theme.getId(),
                theme.getName(),
                theme.getThumbnailUrl(),
                rank
        );
    }
}
