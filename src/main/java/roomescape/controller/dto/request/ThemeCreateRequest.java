package roomescape.controller.dto.request;

import roomescape.domain.Theme;
import roomescape.exception.custom.InvalidRequestArgumentException;

public record ThemeCreateRequest(
        String name,
        String description,
        String thumbnailUrl
) {

    public ThemeCreateRequest {
        validate(name, description, thumbnailUrl);
    }

    public Theme toEntity() {
        return new Theme(name, description, thumbnailUrl);
    }

    private void validate(String name, String description, String thumbnailUrl) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestArgumentException("테마 이름은 비어 있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidRequestArgumentException("테마 설명은 비어 있을 수 없습니다.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new InvalidRequestArgumentException("테마 썸네일은 비어 있을 수 없습니다.");
        }
    }
}
