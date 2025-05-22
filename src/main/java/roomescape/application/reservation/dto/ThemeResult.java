package roomescape.application.reservation.dto;

import roomescape.domain.reservation.Theme;

public record ThemeResult(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public static ThemeResult from(final Theme theme) {
        return new ThemeResult(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
