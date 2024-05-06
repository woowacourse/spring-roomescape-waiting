package roomescape.application.reservation.dto.response;

import roomescape.domain.reservation.Theme;

public record ThemeResponse(long id, String name, String description, String thumbnail) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }
}
