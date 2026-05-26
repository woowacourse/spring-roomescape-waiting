package roomescape.controller.dto;

import roomescape.service.dto.PopularThemeResult;

public record PopularThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        long reservationCount
) {
    public static PopularThemeResponse from(PopularThemeResult result) {
        return new PopularThemeResponse(
                result.theme().id(),
                result.theme().name(),
                result.theme().description(),
                result.theme().thumbnailUrl(),
                result.reservationCount()
        );
    }
}
