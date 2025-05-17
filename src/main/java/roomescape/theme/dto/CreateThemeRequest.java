package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.theme.domain.Theme;

public record CreateThemeRequest(
        @NotBlank(message = "테마명을 입력해주세요.") String name,
        @NotBlank(message = "테마소개를 입력해주세요.") String description,
        @NotBlank(message = "썸네일을 입력해주세요.") String thumbnail
) {

    public Theme convertToTheme() {
        return new Theme(null, name, description, thumbnail);
    }
}
