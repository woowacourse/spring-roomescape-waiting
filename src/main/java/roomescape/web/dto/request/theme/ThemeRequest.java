package roomescape.web.dto.request.theme;

import jakarta.validation.constraints.NotBlank;

import roomescape.domain.Theme;

public record ThemeRequest(
        @NotBlank(message = "이름은 빈값을 허용하지 않습니다.") String name,
        @NotBlank(message = "설명은 빈값을 허용하지 않습니다.") String description,
        @NotBlank(message = "썸내일은 빈값을 허용하자 않습니다.") String thumbnail) {

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
