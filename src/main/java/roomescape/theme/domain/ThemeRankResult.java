package roomescape.theme.domain;

public record ThemeRankResult(
        Long id,
        String name,
        String url,
        Integer rank
) {
    public static ThemeRankResult of(Long id, String name, String url, int rank) {
        return new ThemeRankResult(
                id,
                name,
                url,
                rank
        );
    }
}
