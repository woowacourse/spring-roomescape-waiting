package roomescape.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.Theme;

public record ThemeCreateRequest(
        @NotBlank(message = "이름에 빈값을 입력할 수 없습니다.")
        String name,
        @NotBlank(message = "설명에 빈값을 입력할 수 없습니다.")
        String description,
        @NotBlank(message = "썸네일에 빈값을 입력할 수 없습니다.")
        String thumbnail
) {
    public Theme toRoomTheme() {
        return new Theme(name, description, thumbnail);
    }
}
