package roomescape.theme.application.dto.response;

import roomescape.theme.domain.Theme;

public record ThemeSaveResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        int price
) {
    public ThemeSaveResponse(Long id, String name, String description, String thumbnailUrl) {
        this(id, name, description, thumbnailUrl, 0);
    }

    public static ThemeSaveResponse from(Theme theme) {
        return new ThemeSaveResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                theme.getPrice()
        );
    }
}
