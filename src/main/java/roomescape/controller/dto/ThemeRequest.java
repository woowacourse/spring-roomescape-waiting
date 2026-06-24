package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ThemeRequest(
        @NotBlank(message = "테마의 이름이 입력되지 않았습니다. 테마 이름을 입력해주세요.")
        String name,
        @NotBlank(message = "테마의 설명이 입력되지 않았습니다. 테마 설명을 입력해주세요.")
        String description,
        @NotBlank(message = "테마의 썸네일 경로가 입력되지 않았습니다. 썸네일 경로를 입력해주세요.")
        String thumbnailUrl,
        @NotNull(message = "테마 가격이 입력되지 않았습니다. 테마 가격을 입력해주세요.")
        @PositiveOrZero(message = "테마 가격은 0 이상이어야 합니다.")
        Long price
) {
}
