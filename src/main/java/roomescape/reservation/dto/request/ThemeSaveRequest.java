package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.reservation.domain.Theme;

public record ThemeSaveRequest(
        @NotBlank(message = "테마 이름은 비어있을 수 없습니다.")
        String name,
        String description,
        String thumbnail) {

    public Theme toModel() {
        return new Theme(name, description, thumbnail);
    }
}
