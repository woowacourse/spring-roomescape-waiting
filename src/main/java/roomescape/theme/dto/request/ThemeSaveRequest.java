package roomescape.theme.dto.request;

import roomescape.theme.Theme;

public record ThemeSaveRequest(
        String name,
        String description,
        String thumbnailUrl
) {
    public Theme toDomain() {
        return new Theme(
                null,
                this.name,
                this.description,
                this.thumbnailUrl
        );
    }
}
