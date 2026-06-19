package roomescape.controller.dto;

import roomescape.service.dto.ThemeResult;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        Long price
) {
    public static ThemeResponse from(ThemeResult result) {
        return new ThemeResponse(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailUrl(),
                result.price()
        );
    }
}
