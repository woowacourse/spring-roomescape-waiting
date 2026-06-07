package roomescape.application.theme.response;

import java.util.List;
import roomescape.domain.theme.ThemeRankResult;

public record PopularThemesResponse(
        List<ThemeWithRankPayload> popularThemes
) {

    public static PopularThemesResponse from(List<ThemeRankResult> results) {
        List<ThemeWithRankPayload> payloads = results.stream()
                .map(ThemeWithRankPayload::from)
                .toList();

        return new PopularThemesResponse(payloads);
    }

    private record ThemeWithRankPayload(
            Long id,
            String name,
            String thumbnailUrl,
            Integer rank
    ) {

        private static ThemeWithRankPayload from(ThemeRankResult theme) {
            return new ThemeWithRankPayload(
                    theme.id(),
                    theme.name(),
                    theme.thumbnailUrl(),
                    theme.rank()
            );
        }
    }
}
