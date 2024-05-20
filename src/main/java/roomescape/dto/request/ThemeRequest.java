package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.Theme;
import roomescape.domain.Thumbnail;

public record ThemeRequest(@NotNull(message = "테마명은 비어있을 수 없습니다.") @NotBlank(message = "테마명은 비어있을 수 없습니다.") String name,
                           @NotNull(message = "테마 설명은 비어있을 수 없습니다.") @NotBlank(message = "테마 설명은 비어있을 수 없습니다.") String description,
                           @NotNull(message = "썸네일은 비어있을 수 없습니다.") @NotBlank(message = "썸네일은 비어있을 수 없습니다.") String thumbnail) {

    public Theme toEntity() {
        return new Theme(null, name, description, new Thumbnail(thumbnail));
    }
}
