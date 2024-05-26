package roomescape.member.dto.response;

import roomescape.reservation.model.Theme;

public record CreateThemeOfReservationResponse(Long id,
                                               String name) {
    public static CreateThemeOfReservationResponse from(Theme theme) {
        return new CreateThemeOfReservationResponse(theme.getId(), theme.getName());
    }
}
