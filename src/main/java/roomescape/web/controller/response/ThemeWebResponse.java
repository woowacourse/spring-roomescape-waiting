package roomescape.web.controller.response;

import roomescape.service.response.ThemeAppResponse;

public record ThemeWebResponse(Long id, String name, String description, String thumbnail) {

    public static ThemeWebResponse from(ThemeAppResponse appResponse) {
        return new ThemeWebResponse(
            appResponse.id(),
            appResponse.name(),
            appResponse.description(),
            appResponse.thumbnail()
        );
    }
}
