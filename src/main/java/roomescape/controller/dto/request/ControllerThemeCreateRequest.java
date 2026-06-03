package roomescape.controller.dto.request;


import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
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
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
        if (description == null || description.isBlank()) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
    }
}
