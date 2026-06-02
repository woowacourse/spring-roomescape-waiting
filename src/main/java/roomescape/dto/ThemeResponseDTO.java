package roomescape.dto;

import roomescape.domain.Theme;

public record ThemeResponseDTO(
        Long id,
        String name,
        String description,
        String imageUrl,
        Long runningTime
) {

    public static ThemeResponseDTO from(Theme theme) {
        return new ThemeResponseDTO(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getImageUrl(),
                theme.getRunningTime()
        );
    }
}
