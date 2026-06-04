package roomescape.controller.dto.response;

import roomescape.service.dto.PopularTheme;

import java.util.List;

public record PopularThemesResponse(
        List<PopularThemeResponse> themes
) {

    public static PopularThemesResponse from(List<PopularTheme> popularThemes) {
        return new PopularThemesResponse(popularThemes.stream()
                .map(PopularThemeResponse::from)
                .toList()
        );
    }
}
