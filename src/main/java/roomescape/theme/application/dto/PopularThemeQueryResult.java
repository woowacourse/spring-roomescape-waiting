package roomescape.theme.application.dto;

import roomescape.theme.domain.repository.PopularTheme;

public record PopularThemeQueryResult(Long id, String name, String description, String thumbnailImgUrl,
                                      int reservedCount) {

    public static PopularThemeQueryResult from(PopularTheme popularTheme) {
        return new PopularThemeQueryResult(
                popularTheme.id(),
                popularTheme.name(),
                popularTheme.description(),
                popularTheme.thumbnailImgUrl(),
                popularTheme.reservedCount()
        );
    }
}
