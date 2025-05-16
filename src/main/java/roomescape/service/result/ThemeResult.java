package roomescape.service.result;

import roomescape.domain.Theme;

public record ThemeResult(Long id, String name, String description, String thumbnail) {

    public static ThemeResult from(final Theme theme) {
        return new ThemeResult(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
