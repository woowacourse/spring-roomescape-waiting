package roomescape.controller.api.dto.request;

import roomescape.service.dto.input.ThemeInput;

public record ThemeCreateRequest(String name, String description, String thumbnail) {

    public ThemeInput toInput() {
        return new ThemeInput(name, description, thumbnail);
    }
}
