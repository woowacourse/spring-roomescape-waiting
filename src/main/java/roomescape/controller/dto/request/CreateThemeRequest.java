package roomescape.controller.dto.request;

import roomescape.service.dto.param.CreateThemeParam;

public record CreateThemeRequest(String name, String description, String thumbnail) {

    public CreateThemeParam toServiceParam() {
        return new CreateThemeParam(name, description, thumbnail);
    }
}
