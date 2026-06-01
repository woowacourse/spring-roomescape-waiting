package roomescape.feature.theme.dto.request;

import jakarta.validation.constraints.NotNull;

public record ThemeCreateRequestDto(
    @NotNull(message = "테마 이름은 필수입니다.")
    String name,

    @NotNull(message = "테마 설명은 필수입니다.")
    String description,

    @NotNull(message = "테마 이미지 URL은 필수입니다.")
    String imageUrl
) {

}
