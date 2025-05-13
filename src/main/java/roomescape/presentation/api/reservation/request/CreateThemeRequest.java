package roomescape.presentation.api.reservation.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.application.reservation.dto.CreateThemeParam;

public record CreateThemeRequest(
        @NotBlank(message = "name은 필수입니다.")
        String name,
        @NotBlank(message = "description은 필수입니다.")
        String description,
        @NotBlank(message = "thumbnail은 필수입니다.")
        String thumbnail
) {

    public CreateThemeParam toServiceParam() {
        return new CreateThemeParam(name, description, thumbnail);
    }
}
