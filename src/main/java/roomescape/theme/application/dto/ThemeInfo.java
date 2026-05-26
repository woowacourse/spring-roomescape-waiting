package roomescape.theme.application.dto;

import java.time.LocalTime;
import lombok.Builder;
import roomescape.theme.domain.Theme;

@Builder
public record ThemeInfo(
        Long id,
        String name,
        String thumbnailImageUrl,
        String description,
        LocalTime durationTime
) {
    public static ThemeInfo from(final Theme theme) {
        return ThemeInfo.builder()
                .id(theme.getId())
                .name(theme.getName())
                .thumbnailImageUrl(theme.getThumbnailImageUrl())
                .description(theme.getDescription())
                .durationTime(theme.getDurationTime())
                .build();
    }
}
