package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.theme.domain.Theme;

public record ThemeAddRequest(
        @NotBlank(message = "테마 이름은 필수입니다.") String name,
        @NotBlank(message = "테마 설명은 필수입니다.") String description,
        @NotBlank(message = "테마 썸네일 경로는 필수입니다.") String thumbnail) {

    public Theme toTheme() {
        return new Theme(null, name, description, thumbnail);
    }
}
