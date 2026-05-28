package roomescape.theme.application.dto;

import java.time.LocalTime;
import lombok.Builder;
import roomescape.theme.domain.Theme;

@Builder
public record ThemeCommand(
        String name,
        String thumbnailImageUrl,
        String description,
        LocalTime durationTime
) {
    public Theme toEntity() {
        return Theme.builder()
                .name(this.name)
                .thumbnailImageUrl(this.thumbnailImageUrl)
                .description(this.description)
                .durationTime(this.durationTime)
                .build();
    }
}
