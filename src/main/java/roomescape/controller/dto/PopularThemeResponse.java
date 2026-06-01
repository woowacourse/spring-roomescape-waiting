package roomescape.controller.dto;

import roomescape.repository.result.PopularThemeResult;

public record PopularThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail,
        Long reservationCount
) {
    public static PopularThemeResponse from(PopularThemeResult popularThemeResult) {
        return new PopularThemeResponse(
                popularThemeResult.id(),
                popularThemeResult.name(),
                popularThemeResult.description(),
                popularThemeResult.thumbnail(),
                popularThemeResult.reservationCount()
        );
    }
}
