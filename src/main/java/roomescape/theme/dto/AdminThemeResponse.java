package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record AdminThemeResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        int price
) {

    public static AdminThemeResponse from(Theme saved) {
        return new AdminThemeResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getImageUrl(),
                saved.getPrice()
        );
    }
}