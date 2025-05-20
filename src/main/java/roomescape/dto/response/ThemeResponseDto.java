package roomescape.dto.response;

import roomescape.model.Theme;

public record ThemeResponseDto(
        Long id,
        String name,
        String description,
        String thumbnail
) {
    public ThemeResponseDto(Theme theme) {
        this(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
