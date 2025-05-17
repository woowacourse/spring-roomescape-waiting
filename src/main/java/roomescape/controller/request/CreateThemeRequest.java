package roomescape.controller.request;

import roomescape.service.param.CreateThemeParam;

public record CreateThemeRequest(String name, String description, String thumbnail) {

    public CreateThemeParam toServiceParam() {
        return new CreateThemeParam(name, description, thumbnail);
    }
}
