package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateThemeRequest(
    @NotBlank(message = "테마 이름은 비어있을 수 없습니다.")
    String name,
    @NotBlank(message = "테마 설명은 비어있을 수 없습니다.")
    String description,
    @NotBlank(message = "테마 썸네일은 비어있을 수 없습니다.")
    String thumbnail) {

}
