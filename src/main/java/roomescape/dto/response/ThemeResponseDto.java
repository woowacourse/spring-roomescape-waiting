package roomescape.dto.response;

import roomescape.domain.theme.Theme;

public record ThemeResponseDto(
        Long id,
        String name,
        String thumbnailUrl,
        String description,
        long price
) {
    public static ThemeResponseDto from(Theme theme) {
        return new ThemeResponseDto(
                theme.getId(),
                theme.getName().getValue(),
                theme.getThumbnailUrl(),
                theme.getDescription(),
                theme.getPrice()
        );
    }
}
