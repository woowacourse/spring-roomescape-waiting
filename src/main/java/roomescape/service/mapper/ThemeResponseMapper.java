package roomescape.service.mapper;

import roomescape.domain.Theme;
import roomescape.dto.ThemeResponse;

public class ThemeResponseMapper {
    public static ThemeResponse toResponse(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
