package roomescape.dto.response;

import roomescape.domain.Theme;

public record ThemeResponseDto(Long id,
                               String name,
                               String description,
                               String thumbnail) {

    public static ThemeResponseDto of(Theme theme) {
        return new ThemeResponseDto(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
