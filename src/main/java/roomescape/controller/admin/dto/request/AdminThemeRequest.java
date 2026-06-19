package roomescape.controller.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.service.command.ThemeRegisterCommand;

public record AdminThemeRequest(
        @NotBlank(message = "이름은 필수 값입니다.")
        String name,

        @NotBlank(message = "설명은 필수 값입니다.")
        String description,

        @NotBlank(message = "썸네일 이미지는 필수 값입니다.")
        String thumbnailImageUrl,

        @NotNull(message = "금액은 필수 값입니다.")
        @Positive(message = "금액은 양수여야 합니다.")
        Long price
) {

    public ThemeRegisterCommand toCommand() {
        return new ThemeRegisterCommand(name, description, thumbnailImageUrl, price);
    }
}
