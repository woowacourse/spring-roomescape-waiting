package roomescape.controller.dto.response;

import roomescape.service.dto.PopularTheme;

public record PopularThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail,
        int rank
) {
    public static PopularThemeResponse from(PopularTheme popularTheme) {
        return new PopularThemeResponse(
                popularTheme.id(),
                popularTheme.name(),
                popularTheme.description(),
                popularTheme.thumbnail(),
                popularTheme.rank()
        );
    }
}
