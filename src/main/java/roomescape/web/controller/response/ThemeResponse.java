package roomescape.web.controller.response;

import roomescape.service.response.ThemeAppResponse;

public record ThemeResponse(Long id, String name, String description, String thumbnail) {

    public static ThemeResponse from(ThemeAppResponse appResponse) {
        return new ThemeResponse(
                appResponse.id(),
                appResponse.name(),
                appResponse.description(),
                appResponse.thumbnail()
        );
    }
}
