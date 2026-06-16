package roomescape.admin.theme.dto;

import roomescape.domain.theme.Theme;

public record AdminThemeResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        long price
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
