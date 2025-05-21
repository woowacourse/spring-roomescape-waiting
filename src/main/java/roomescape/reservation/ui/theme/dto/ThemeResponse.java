package roomescape.reservation.ui.theme.dto;

import roomescape.reservation.application.theme.dto.ThemeInfo;

public record ThemeResponse(long id, String name, String description, String thumbnail) {

    public ThemeResponse(final ThemeInfo themeInfo) {
        this(themeInfo.id(), themeInfo.name(), themeInfo.description(), themeInfo.thumbnail());
    }
}
