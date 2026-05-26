package roomescape.dto.response;

import roomescape.domain.reservation.theme.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String url
) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName().value(),
                theme.getDescription().value(),
                theme.getUrl().value()
        );
    }
}
