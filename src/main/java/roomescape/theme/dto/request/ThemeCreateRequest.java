package roomescape.theme.dto.request;

import roomescape.theme.domain.Theme;

public record ThemeCreateRequest(
        String name,
        String description,
        String thumbnail
) {
    public ThemeCreateRequest {
        if (name == null) {
            throw new IllegalArgumentException("이름은 null일 수 없습니다.");
        }
        if (description == null) {
            throw new IllegalArgumentException("설명은 null일 수 없습니다.");
        }
        if (thumbnail == null) {
            throw new IllegalArgumentException("썸네일은 null일 수 없습니다.");
        }
    }

    public Theme toTheme() {
        return Theme.withUnassignedId(name, description, thumbnail);
    }
}
