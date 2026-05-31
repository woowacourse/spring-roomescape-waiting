package roomescape.theme.presentation.dto;

import lombok.Builder;
import roomescape.theme.application.dto.ThemeInfo;

@Builder
public record ThemeResponse(
        Long id,
        String name,
        String thumbnailImageUrl,
        String description,
        boolean isActive
) {
    public static ThemeResponse from(ThemeInfo theme) {
        return ThemeResponse.builder()
                .id(theme.id())
                .name(theme.name())
                .thumbnailImageUrl(theme.thumbnailImageUrl())
                .description(theme.description())
                .isActive(theme.isActive())
                .build();
    }
}
