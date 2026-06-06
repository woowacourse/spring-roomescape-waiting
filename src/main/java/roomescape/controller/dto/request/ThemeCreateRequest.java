package roomescape.controller.dto.request;

import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;

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
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_NAME_NULL);
        }
        if (description == null || description.isBlank()) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_DESCRIPTION_NULL);
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_THUMBNAIL_NULL);
        }
    }
}
