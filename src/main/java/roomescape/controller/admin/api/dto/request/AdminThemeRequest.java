package roomescape.controller.admin.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import roomescape.service.command.ThemeRegisterCommand;

public record AdminThemeRequest(
        @NotBlank(message = "이름은 필수 값입니다.")
        String name,

        @NotBlank(message = "설명은 필수 값입니다.")
        String description,

        @NotBlank(message = "썸네일 이미지는 필수 값입니다.")
        String thumbnailImageUrl,

        @NotNull(message = "가격은 필수 값입니다.")
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        Integer price
) {
    public AdminThemeRequest(String name, String description, String thumbnailImageUrl) {
        this(name, description, thumbnailImageUrl, 0);
    }

    public ThemeRegisterCommand toCommand() {
        return new ThemeRegisterCommand(name, description, thumbnailImageUrl, price);
    }
}
