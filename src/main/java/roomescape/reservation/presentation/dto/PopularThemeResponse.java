package roomescape.reservation.presentation.dto;

import roomescape.reservation.domain.Theme;

public record PopularThemeResponse(String name, String description, String thumbnail) {

    public static PopularThemeResponse from(Theme theme) {
        return new PopularThemeResponse(theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
