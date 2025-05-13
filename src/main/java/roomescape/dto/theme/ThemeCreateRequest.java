package roomescape.dto.theme;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import roomescape.domain.Theme;

public record ThemeCreateRequest(
        @NotBlank(message = "[ERROR] 테마의 이름은 1글자 이상으로 이루어져야 합니다.") String name,
        @NotBlank(message = "[ERROR] 테마 설명이 없습니다.") String description,
        @NotBlank(message = "[ERROR] 테마 이미지가 없습니다.")
        @URL(message = "[ERROR] 썸네일 이미지가 URL 형식이 아닙니다.", protocol = "https") String thumbnail
) {

    public Theme createWithoutId() {
        return new Theme(null, name, description, thumbnail);
    }
}
