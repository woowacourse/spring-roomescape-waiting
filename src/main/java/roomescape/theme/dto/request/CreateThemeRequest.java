package roomescape.theme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.theme.model.Theme;

public record CreateThemeRequest(
        @NotBlank(message = "테마 명은 공백 문자가 불가능합니다.")
        @Size(message = "테마 명은 최대 255자까지 입력이 가능합니다.", max = 255)
        String name,

        @NotBlank(message = "테마 설명은 공백 문자가 불가능합니다.")
        @Size(message = "테마 설명은 최대 255자까지 입력이 가능합니다.", max = 255)
        String description,

        @NotBlank(message = "테마 썸네일은 공백 문자가 불가능합니다.")
        @Size(message = "테마 썸네일은 최대 255자까지 입력이 가능합니다.", max = 255)
        String thumbnail) {
    public Theme toTheme() {
        return new Theme(null, name, description, thumbnail);
    }
}
