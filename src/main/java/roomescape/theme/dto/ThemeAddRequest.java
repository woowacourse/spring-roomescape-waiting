package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.theme.domain.Theme;

public record ThemeAddRequest(
        @NotBlank(message = "테마 이름은 필수입니다.")
        @Size(max = 40, message = "테마 이름의 길이를 확인해주세요")
        String name,

        @NotBlank(message = "테마 설명은 필수입니다.")
        @Size(max = 80, message = "테마 설명의 길이를 확인해주세요")
        String description,

        @NotBlank(message = "테마 썸네일 경로는 필수입니다.")
        String thumbnail
) {

    public Theme toTheme() {
        return new Theme(null, name, description, thumbnail);
    }
}
