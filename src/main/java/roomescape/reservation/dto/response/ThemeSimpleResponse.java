package roomescape.reservation.dto.response;

import roomescape.theme.domain.Theme;

public record ThemeSimpleResponse(
        Long id,
        String name
) {

    public static ThemeSimpleResponse from(Theme theme) {
        return new ThemeSimpleResponse(theme.getId(), theme.getName());
    }
}
