package roomescape.theme.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.theme.domain.Theme;

public record ThemeCreateRequest(
    @NotBlank(message = "이름은 빈 값이 올 수 없습니다")
    String name,
    String description,
    String thumbnail
) {
    public Theme toTheme() {
        return new Theme(null, name, description, thumbnail);
    }
}
