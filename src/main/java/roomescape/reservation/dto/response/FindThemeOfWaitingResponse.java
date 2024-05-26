package roomescape.reservation.dto.response;

import roomescape.reservation.model.Theme;

public record FindThemeOfWaitingResponse(Long id, String name, String description, String thumbnail) {

    public static FindThemeOfWaitingResponse from(Theme theme) {
        return new FindThemeOfWaitingResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());
    }
}
