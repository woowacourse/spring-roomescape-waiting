package roomescape.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ThemeRequest(
        @Size(max = 255, message = "255자 이하의 테마 이름을 입력해주세요.")
        @NotBlank(message = "공백은 불가능합니다. 테마 이름을 입력해주세요.")
        String name,
        @Size(max = 255, message = "255자 이하의 테마 설명을 입력해주세요.")
        @NotBlank(message = "공백은 불가능합니다. 테마 설명을 입력해주세요.")
        String description,
        @NotBlank(message = "공백은 불가능합니다. 이미지 url을 입력해주세요.")
        String thumbnailUrl,
        @NotNull(message = "금액을 입력해주세요.")
        @Positive(message = "금액은 양수여야 합니다.")
        Long amount) {
}
