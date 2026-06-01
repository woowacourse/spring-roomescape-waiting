package roomescape.controller.dto;

import roomescape.domain.Theme;

public record ReservationWaitingThemeResponse(
        Long id,
        String name
) {
    public static ReservationWaitingThemeResponse from(Theme theme) {
        return new ReservationWaitingThemeResponse(
                theme.getId(),
                theme.getName());
    }
}
