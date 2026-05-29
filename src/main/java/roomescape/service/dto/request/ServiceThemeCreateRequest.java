package roomescape.service.dto.request;

import roomescape.domain.Theme;

public record ServiceThemeCreateRequest(
        String name,
        String description,
        String thumbnailUrl
) {
    public Theme toEntity() {
        return new Theme(name, description, thumbnailUrl);
    }
}
