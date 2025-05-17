package roomescape.reservation.controller.dto;

import roomescape.reservation.service.dto.ThemeInfo;

public record ThemeResponse(long id, String name, String description, String thumbnail) {

    public ThemeResponse(final ThemeInfo themeInfo) {
        this(themeInfo.id(), themeInfo.name(), themeInfo.description(), themeInfo.thumbnail());
    }
}
