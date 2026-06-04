package roomescape.controller.dto.response;

import roomescape.service.dto.result.ThemeResult;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String url
) {

    public static ThemeResponse from(ThemeResult result) {
        return new ThemeResponse(
                result.id(),
                result.name(),
                result.description(),
                result.url()
        );
    }
}
