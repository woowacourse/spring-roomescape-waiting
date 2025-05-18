package roomescape.reservation.application.dto;

import roomescape.reservation.domain.theme.Theme;

public record ThemeInfo(long id, String name, String description, String thumbnail) {

    public ThemeInfo(final Theme theme) {
        this(theme.id(), theme.themeName().name(), theme.themeDescription().description(),
                theme.themeThumbnail().thumbnail());
    }
}
