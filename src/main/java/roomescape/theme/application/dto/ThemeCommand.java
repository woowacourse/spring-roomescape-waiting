package roomescape.theme.application.dto;

import lombok.Builder;
import roomescape.theme.domain.Theme;

@Builder
public record ThemeCommand(
        String name,
        String thumbnailImageUrl,
        String description
) {
    public Theme toEntity() {
        return Theme.create(name, thumbnailImageUrl, description);
    }
}
