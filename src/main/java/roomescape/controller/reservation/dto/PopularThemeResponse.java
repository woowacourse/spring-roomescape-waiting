package roomescape.controller.reservation.dto;

import roomescape.domain.Theme;

public record PopularThemeResponse(String name, String thumbnail, String description) {

    public static PopularThemeResponse from(final Theme theme) {
        return new PopularThemeResponse(theme.getName(), theme.getThumbnail(), theme.getDescription());
    }
}
