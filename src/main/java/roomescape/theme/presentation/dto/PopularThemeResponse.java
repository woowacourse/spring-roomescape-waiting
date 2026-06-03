package roomescape.theme.presentation.dto;

import roomescape.theme.domain.repository.PopularTheme;

public record PopularThemeResponse(Long id, String name, String description, String thumbnailImgUrl,
                                   int reservedCount) {

    public static PopularThemeResponse from(PopularTheme popularTheme) {
        return new PopularThemeResponse(popularTheme.id(), popularTheme.name(), popularTheme.description(),
                popularTheme.thumbnailImgUrl(), popularTheme.reservedCount());
    }
}
