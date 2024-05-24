package roomescape.web.controller.response;

import roomescape.service.response.ThemeDto;

public record ThemeResponse(Long id, String name, String description, String thumbnail) {

    public static ThemeResponse from(ThemeDto appResponse) {
        return new ThemeResponse(
                appResponse.id(),
                appResponse.name(),
                appResponse.description(),
                appResponse.thumbnail()
        );
    }
}
