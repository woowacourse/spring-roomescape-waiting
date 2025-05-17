package roomescape.dto.theme;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.theme.Theme;

public record AddThemeDto(@NotBlank(message = "테마 이름은 비어있을 수 없습니다.") String name,
                          @NotBlank(message = "설명은 비어있을 수 없습니다.") String description,
                          @NotBlank(message = "썸네일은 비어있을 수 없습니다.") String thumbnail) {

    public Theme toEntity() {
        return new Theme(null, name, description, thumbnail);
    }
}
