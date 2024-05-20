package roomescape.member.dto.response;

import roomescape.theme.model.Theme;

public record CreateThemeOfReservationResponse(Long id,
                                               String name) {
    public static CreateThemeOfReservationResponse from(final Theme theme) {
        return new CreateThemeOfReservationResponse(theme.getId(), theme.getName());
    }
}
