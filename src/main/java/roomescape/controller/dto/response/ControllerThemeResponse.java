package roomescape.controller.dto.response;

import roomescape.service.dto.response.ServiceThemeResponse;

public record ControllerThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ControllerThemeResponse from(ServiceThemeResponse response) {
        return new ControllerThemeResponse(
                response.id(),
                response.name(),
                response.description(),
                response.thumbnailUrl()
        );
    }
}
