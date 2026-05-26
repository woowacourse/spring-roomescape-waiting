package roomescape.dto.response;

import roomescape.domain.reservation.theme.Theme;
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

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName().value(),
                theme.getDescription().value(),
                theme.getUrl().value()
        );
    }
}
