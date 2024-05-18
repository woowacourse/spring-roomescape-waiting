package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Theme;

public record ReservedThemeResponse(Long id, String name) {

    public static ReservedThemeResponse from(Theme theme) {
        return new ReservedThemeResponse(theme.getId(), theme.getName());
    }
}
