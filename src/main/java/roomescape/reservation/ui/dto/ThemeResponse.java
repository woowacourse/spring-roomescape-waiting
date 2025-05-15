package roomescape.reservation.ui.dto;

import roomescape.reservation.application.dto.ThemeInfo;

public record ThemeResponse(long id, String name, String description, String thumbnail) {

    public ThemeResponse(final ThemeInfo themeInfo) {
        this(themeInfo.id(), themeInfo.name(), themeInfo.description(), themeInfo.thumbnail());
    }
}
