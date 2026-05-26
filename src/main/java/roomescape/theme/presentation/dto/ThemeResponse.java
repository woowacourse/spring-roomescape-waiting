package roomescape.theme.presentation.dto;

import java.time.LocalTime;
import lombok.Builder;
import roomescape.theme.application.dto.ThemeInfo;

@Builder
public record ThemeResponse(
        Long id,
        String name,
        String thumbnailImageUrl,
        String description,
        LocalTime durationTime
) {
    public static ThemeResponse from(final ThemeInfo theme) {
        return ThemeResponse.builder()
                .id(theme.id())
                .name(theme.name())
                .thumbnailImageUrl(theme.thumbnailImageUrl())
                .description(theme.description())
                .durationTime(theme.durationTime())
                .build();
    }
}
