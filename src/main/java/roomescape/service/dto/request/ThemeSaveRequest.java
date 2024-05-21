package roomescape.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.domain.Theme;

public record ThemeSaveRequest(
        @NotBlank(message = "테마 이름을 입력해주세요")
        @Size(max = 30, message = "테마 이름은 30자까지 가능합니다.")
        String name,

        @NotBlank(message = "테마 설명을 입력해주세요")
        @Size(max = 255, message = "테마 설명은 255자까지 가능합니다.")
        String description,

        @NotBlank(message = "테마 썸네일 링크를 입력해주세요")
        @Size(max = 255, message = "썸네일 링크는 255자까지 가능합니다.")
        String thumbnail
) {

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
