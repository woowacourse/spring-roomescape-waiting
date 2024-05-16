package roomescape.reservation.dto;

import roomescape.reservation.model.Theme;
import roomescape.reservation.model.ThemeDescription;
import roomescape.reservation.model.ThemeName;
import roomescape.reservation.model.ThemeThumbnail;

public record ThemeDto(
        Long id,
        ThemeName name,
        ThemeDescription description,
        ThemeThumbnail thumbnail
) {
    public static ThemeDto from(final Theme theme) {
        return new ThemeDto(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
