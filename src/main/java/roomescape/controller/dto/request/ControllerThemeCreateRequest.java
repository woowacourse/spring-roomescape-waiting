package roomescape.controller.dto.request;

import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.service.dto.request.ServiceThemeCreateRequest;

public record ControllerThemeCreateRequest(
        String name,
        String description,
        String thumbnailUrl
) {

    public ControllerThemeCreateRequest {
        validate(name, description, thumbnailUrl);
    }

    public ServiceThemeCreateRequest toServiceThemeRequest() {
        return new ServiceThemeCreateRequest(name, description, thumbnailUrl);
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
