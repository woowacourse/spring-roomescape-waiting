package roomescape.theme.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ThemeRequestDto(
        @NotBlank @Size(max = 40, message = "이름은 40자 이하여야 합니다")
        String name,
        @URL
        String thumbnailUrl,
        @Size(max = 200, message = "설명은 최대 200자 까지 가능합니다.")
        String description,
        @NotNull @Positive(message = "가격은 0보다 커야 합니다.")
        Long price
) {
}
