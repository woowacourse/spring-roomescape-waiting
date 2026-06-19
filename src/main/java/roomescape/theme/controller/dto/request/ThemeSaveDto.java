package roomescape.theme.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ThemeSaveDto(

        @NotBlank(message = "name은 비어있을 수 없습니다.")
        String name,

        @NotBlank(message = "description은 비어있을 수 없습니다.")
        String description,

        @NotNull(message = "thumbnailUrl은 필수 입력값입니다.")
        String thumbnailUrl,

        @NotNull(message = "amount는 필수 입력값입니다.")
        @PositiveOrZero(message = "amount는 음수일 수 없습니다.")
        Long amount
) {
}
