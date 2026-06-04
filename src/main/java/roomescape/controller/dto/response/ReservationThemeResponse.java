package roomescape.controller.dto.response;

import roomescape.domain.Theme;

public record ReservationThemeResponse(
        Long id,
        String name
) {

    public static ReservationThemeResponse from(Theme theme) {
        return new ReservationThemeResponse(
                theme.getId(),
                theme.getName()
        );
    }
}
