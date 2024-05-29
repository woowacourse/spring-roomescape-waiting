package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ThemeOutput;

public record ThemeResponse(long id, String name, String description, String thumbnail) {

    public static ThemeResponse from(final ThemeOutput output) {
        return new ThemeResponse(output.id(), output.name(), output.description(), output.thumbnail());
    }

}
