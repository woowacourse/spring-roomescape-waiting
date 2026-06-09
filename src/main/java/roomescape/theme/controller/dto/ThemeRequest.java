package roomescape.theme.controller.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import roomescape.theme.service.dto.ThemeCommand;

public record ThemeRequest(

        @NotBlank(message = "테마 이름은 필수입니다.")
        String name,

        @NotBlank(message = "테마 설명은 필수입니다.")
        String description,

        @NotBlank(message = "테마 썸네일 URL은 필수입니다.")
        @URL(message = "올바른 썸네일 URL 형식이 아닙니다.")
        String thumbnailUrl
) {
    public ThemeCommand toCommand() {
        return new ThemeCommand(name, description, thumbnailUrl);
    }
}
