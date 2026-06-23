package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        int price
) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getImageUrl(),
                theme.getPrice()
        );
    }
}