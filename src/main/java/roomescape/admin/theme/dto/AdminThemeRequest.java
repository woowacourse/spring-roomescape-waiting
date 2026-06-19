package roomescape.admin.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminThemeRequest(
        @NotBlank(message = "등록할 테마의 이름은 필수 입력 값입니다.")
        @Size(max = 255, message = "이름은 255자를 초과할 수 없습니다.")
        String name,

        @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
        String description,

        @Size(max = 255, message = "URL은 255자를 초과할 수 없습니다.")
        String imageUrl,

        @Positive(message = "가격은 양수여야 합니다.")
        long price
) {

}
