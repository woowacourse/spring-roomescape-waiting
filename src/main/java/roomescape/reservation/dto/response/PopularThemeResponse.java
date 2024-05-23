package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Theme;

public record PopularThemeResponse(
        String name,
        String description,
        String thumbnail
) {

    public PopularThemeResponse(Theme theme) {
        this(theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
