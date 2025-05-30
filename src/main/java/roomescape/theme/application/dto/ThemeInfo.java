package roomescape.theme.application.dto;

import roomescape.theme.domain.Theme;

public record ThemeInfo(long id, String name, String description, String thumbnail) {

    public ThemeInfo(final Theme theme) {
        this(theme.id(), theme.getNameOfTheme(), theme.getDescriptionOfTheme(), theme.getThumbnailOfTheme());
    }
}
