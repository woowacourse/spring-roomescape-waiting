package roomescape.reservation.dto.response;

import roomescape.reservation.model.Theme;

public record CreateThemeOfWaitingResponse(Long id, String name, String description, String thumbnail) {

    public static CreateThemeOfWaitingResponse from(final Theme theme) {
        return new CreateThemeOfWaitingResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());
    }
}
