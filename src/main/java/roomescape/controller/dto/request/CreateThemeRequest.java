package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.service.dto.param.CreateThemeParam;

public record CreateThemeRequest(

        @NotBlank
        String name,

        @NotBlank
        String description,

        @NotBlank
        String thumbnail
) {

    public CreateThemeParam toServiceParam() {
        return new CreateThemeParam(name, description, thumbnail);
    }
}
