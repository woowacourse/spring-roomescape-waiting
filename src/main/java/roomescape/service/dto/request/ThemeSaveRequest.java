package roomescape.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.Theme;

public record ThemeSaveRequest(@NotBlank(message = "테마 이름을 입력해주세요.") String name,
                               @NotNull(message = "테마 이름을 입력해주세요.") String description,
                               @NotNull(message = "테마 썸네일을 입력해주세요.") String thumbnail) {

    public Theme toEntity(ThemeSaveRequest request) {
        return new Theme(request.name(), request.description(), request.thumbnail());
    }
}
