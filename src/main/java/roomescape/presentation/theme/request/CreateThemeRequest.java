package roomescape.presentation.theme.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.theme.Theme;

public record CreateThemeRequest(
    @NotBlank(message = "테마 이름은 비어있을 수 없습니다.")
    String name,

    @NotBlank(message = "테마 내용은 비어있을 수 없습니다.")
    String content,

    @NotBlank(message = "테마 URL은 비어있을 수 없습니다.")
    String url
) {

    public Theme toEntity() {
        return Theme.createWithoutId(
            name,
            content,
            url
        );
    }
}
