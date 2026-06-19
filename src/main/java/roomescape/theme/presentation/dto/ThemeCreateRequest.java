package roomescape.theme.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.theme.application.dto.ThemeCreateCommand;

public record ThemeCreateRequest(
        @NotBlank(message = "[ERROR] 테마 이름은 비어있을 수 없습니다.")
        String name,
        @NotBlank(message = "[ERROR] 테마 설명은 비어있을 수 없습니다.")
        String description,
        @NotBlank(message = "[ERROR] 썸네일 이미지 URL은 비어있을 수 없습니다.")
        String thumbnailImgUrl,
        @NotNull(message = "[ERROR] 테마 가격은 비어있을 수 없습니다.")
        Long price
) {
    public ThemeCreateCommand toCommand() {
        return new ThemeCreateCommand(name, description, thumbnailImgUrl, price);
    }
}
