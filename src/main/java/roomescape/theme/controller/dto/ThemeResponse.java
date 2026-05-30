package roomescape.theme.controller.dto;

import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.theme.service.dto.ThemeResult;

public record ThemeResponse(Long id, String name, String description, String thumbnailUrl) {

    public static ThemeResponse from(PopularThemeQueryResult theme) {
        return new ThemeResponse(
                theme.id(),
                theme.name(),
                theme.description(),
                theme.thumbnailUrl()
        );
    }

    public static ThemeResponse from(ThemeResult result) {
        return new ThemeResponse(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailUrl()
        );
    }
}
