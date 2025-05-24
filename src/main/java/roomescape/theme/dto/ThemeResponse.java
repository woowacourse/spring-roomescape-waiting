package roomescape.theme.dto;

import roomescape.exception.custom.reason.ResponseInvalidException;
import roomescape.theme.domain.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {
    public ThemeResponse {
        if (id == null || name == null || description == null || thumbnail == null) {
            throw new ResponseInvalidException();
        }
    }

    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
