package roomescape.controller.request;

import jakarta.validation.constraints.NotNull;
import roomescape.service.param.CreateThemeParam;

public record CreateThemeRequest(
        @NotNull(message = "테마 이름은 필수 값입니다.")
        String name,

        @NotNull(message = "테마 설명은 필수 값입니다.")
        String description,

        @NotNull(message = "테마 썸네일은 필수 값입니다.")
        String thumbnail) {

    public CreateThemeParam toServiceParam() {
        return new CreateThemeParam(name, description, thumbnail);
    }
}
