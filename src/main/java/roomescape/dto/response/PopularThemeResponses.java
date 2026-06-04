package roomescape.dto.response;

import java.util.List;
import roomescape.domain.PopularTheme;

public record PopularThemeResponses(
        List<PopularThemeResponse> themes
) {
    public static PopularThemeResponses from(List<PopularTheme> popularThemes) {
        return new PopularThemeResponses(
                popularThemes.stream().map(PopularThemeResponse::from).toList()
        );
    }
}
